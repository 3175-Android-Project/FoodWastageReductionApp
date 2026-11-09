import unittest
from unittest.mock import patch

from server import CODE_LIFETIME, RESEND_DELAY, EmailVerification, VerificationError, gmail_delivery


class EmailVerificationTest(unittest.TestCase):
    email = "customer@example.com"

    def setUp(self):
        self.now = 100.0
        self.mailbox = {}
        self.service = EmailVerification(self.mailbox.__setitem__, clock=lambda: self.now)

    def start(self):
        return self.service.request_code(self.email)["challengeId"]

    def expect_error(self, reason, action, *args):
        with self.assertRaises(VerificationError) as failure:
            action(*args)
        self.assertEqual(reason, failure.exception.reason)

    def test_success_only_for_recipient_and_only_once(self):
        token = self.start()
        code = self.mailbox[self.email]
        self.expect_error("invalid_challenge", self.service.verify_code,
                          "another@example.com", token, code)
        self.assertEqual({"verified": True, "email": self.email},
                         self.service.verify_code(self.email, token, code))
        self.expect_error("invalid_challenge", self.service.verify_code, self.email, token, code)

    def test_wrong_code_exhausts_five_attempts(self):
        token = self.start()
        code = self.mailbox[self.email]
        wrong = f"{(int(code) + 1) % 1_000_000:06d}"
        for _ in range(4):
            self.expect_error("invalid_code", self.service.verify_code, self.email, token, wrong)
        self.expect_error("attempts_exhausted", self.service.verify_code, self.email, token, wrong)
        self.expect_error("attempts_exhausted", self.service.verify_code, self.email, token, code)

    def test_code_expires_at_five_minutes(self):
        token = self.start()
        self.now += CODE_LIFETIME
        self.expect_error("expired_code", self.service.verify_code,
                          self.email, token, self.mailbox[self.email])

    def test_resend_has_cooldown_and_replaces_old_challenge(self):
        old_token = self.start()
        old_code = self.mailbox[self.email]
        self.expect_error("too_many_requests", self.start)
        self.now += RESEND_DELAY
        new_token = self.start()
        self.assertNotEqual(old_token, new_token)
        self.expect_error("invalid_challenge", self.service.verify_code, self.email, old_token, old_code)
        self.assertTrue(self.service.verify_code(self.email, new_token, self.mailbox[self.email])["verified"])

    def test_failed_delivery_does_not_replace_valid_code(self):
        token = self.start()
        code = self.mailbox[self.email]
        self.now += RESEND_DELAY
        self.service.deliver = lambda email, code: self.fail_delivery()
        with patch("builtins.print"):
            self.expect_error("delivery_failed", self.start)
        self.assertTrue(self.service.verify_code(self.email, token, code)["verified"])

    def test_smtp_message_contains_code_and_correct_recipient(self):
        with patch("server.smtplib.SMTP") as smtp:
            gmail_delivery("grabbit@example.com", "abcdefghijklmnop")(self.email, "012345")
            connection = smtp.return_value.__enter__.return_value
            message = connection.send_message.call_args.args[0]
            self.assertEqual(self.email, message["To"])
            self.assertIn("012345", message.get_content())
            self.assertIsNotNone(connection.starttls.call_args.kwargs["context"])

    @staticmethod
    def fail_delivery():
        raise OSError("Test SMTP failure")


if __name__ == "__main__":
    unittest.main()
