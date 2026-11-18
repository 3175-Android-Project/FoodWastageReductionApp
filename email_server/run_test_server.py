"""Local instrumentation-test server. No SMTP or real email delivery."""
from unittest.mock import patch
from server import EmailVerification, make_server

if __name__ == "__main__":
    service = EmailVerification(lambda email, code: None)
    print("Local account tests: http://127.0.0.1:8766 (test code 123456)", flush=True)
    with patch("server.secrets.randbelow", return_value=123456):
        make_server(service, port=8766, host="127.0.0.1").serve_forever()
