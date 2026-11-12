const CODE_SECONDS = 300;
const RESEND_SECONDS = 60;
const HOUR_SECONDS = 3600;
const MAX_ATTEMPTS = 5;
const REQUEST_PATH = 'v1/email/request';
const VERIFY_PATH = 'v1/email/verify';

function doGet(event) {
  const path = event && event.pathInfo;
  return json_(!path || path === 'healthz' ? { status: 'ok' } : { error: 'not_found' });
}

function doPost(event) {
  const path = event && ((event.parameter && event.parameter.route) || event.pathInfo);
  if (path !== REQUEST_PATH && path !== VERIFY_PATH) return json_({ error: 'not_found' });
  const body = event.postData;
  if (!body || !body.contents || body.contents.length > 1024 ||
      String(body.type).split(';')[0].trim().toLowerCase() !== 'application/json') {
    return json_({ error: 'invalid_request' });
  }
  let data;
  try {
    data = JSON.parse(body.contents);
  } catch (error) {
    return json_({ error: 'invalid_request' });
  }
  if (!data || typeof data !== 'object' || Array.isArray(data)) return json_({ error: 'invalid_request' });
  const email = typeof data.email === 'string' ? data.email.trim().toLowerCase() : '';
  if (email.length > 254 || !/^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9-]+(?:\.[a-z0-9-]+)+$/.test(email)) {
    return json_({ error: 'invalid_email' });
  }
  const lock = LockService.getScriptLock();
  if (!lock.tryLock(3000)) return json_({ error: 'too_many_requests', retryAfter: 3 });
  try {
    const cache = CacheService.getScriptCache();
    const key = 'code:' + Utilities.base64EncodeWebSafe(
      Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, email));
    const result = path === REQUEST_PATH
      ? requestCode_(cache, key, email)
      : verifyCode_(cache, key, email, data.challengeId, data.code);
    return json_(result);
  } catch (error) {
    console.error('Grabbit verification failed: ' + error.name);
    return json_({ error: 'server_error' });
  } finally {
    lock.releaseLock();
  }
}

function requestCode_(cache, key, email) {
  const now = Date.now();
  const requests = JSON.parse(cache.get('requests') || '[]')
    .filter(request => now - request.at < HOUR_SECONDS * 1000);
  const own = requests.filter(request => request.key === key);
  const last = own[own.length - 1];
  if (last && now - last.at < RESEND_SECONDS * 1000) {
    return { error: 'too_many_requests', retryAfter: Math.ceil(RESEND_SECONDS - (now - last.at) / 1000) };
  }
  if (own.length >= 5 || requests.length >= 30 || MailApp.getRemainingDailyQuota() < 1) {
    return { error: 'too_many_requests', retryAfter: HOUR_SECONDS };
  }

  const challengeId = uuid_();
  let number;
  do {
    number = parseInt(uuid_().slice(0, 8), 16);
  } while (number >= 4294000000);
  const code = String(number % 1000000).padStart(6, '0');
  const digest = digest_(challengeId, code);
  requests.push({ key: key, at: now });
  cache.put('requests', JSON.stringify(requests), HOUR_SECONDS);
  try {
    MailApp.sendEmail({
      to: email,
      name: 'Grabbit',
      subject: 'Your Grabbit verification code',
      body: 'Your Grabbit verification code is ' + code + '.\n\n' +
        'It expires in 5 minutes and can be used once.\n' +
        'If you did not request it, you can ignore this email.'
    });
  } catch (error) {
    console.error('Grabbit mail delivery failed: ' + error.name);
    return { error: 'delivery_failed', retryAfter: RESEND_SECONDS };
  }
  cache.put(key, JSON.stringify({
    id: challengeId,
    digest: digest,
    expires: Date.now() + CODE_SECONDS * 1000,
    attempts: MAX_ATTEMPTS
  }), CODE_SECONDS);
  return { challengeId: challengeId, expiresIn: CODE_SECONDS, retryAfter: RESEND_SECONDS };
}

function verifyCode_(cache, key, email, challengeId, code) {
  if (typeof code !== 'string' || !/^[0-9]{6}$/.test(code)) return { error: 'invalid_code' };
  const saved = cache.get(key);
  if (!saved || typeof challengeId !== 'string') return { error: 'invalid_challenge' };
  const challenge = JSON.parse(saved);
  if (challenge.id !== challengeId) return { error: 'invalid_challenge' };
  const remaining = Math.ceil((challenge.expires - Date.now()) / 1000);
  if (remaining <= 0) {
    cache.remove(key);
    return { error: 'expired_code' };
  }
  if (!challenge.attempts) return { error: 'attempts_exhausted' };
  if (!sameDigest_(challenge.digest, digest_(challengeId, code))) {
    challenge.attempts--;
    cache.put(key, JSON.stringify(challenge), remaining);
    return { error: challenge.attempts ? 'invalid_code' : 'attempts_exhausted' };
  }
  cache.remove(key);
  return { verified: true, email: email };
}

function digest_(challengeId, code) {
  const properties = PropertiesService.getScriptProperties();
  let secret = properties.getProperty('grabbit_code_secret');
  if (!secret) {
    secret = uuid_() + uuid_();
    properties.setProperty('grabbit_code_secret', secret);
  }
  return Utilities.base64EncodeWebSafe(Utilities.computeHmacSha256Signature(challengeId + ':' + code, secret));
}

function sameDigest_(left, right) {
  let difference = left.length ^ right.length;
  for (let index = 0; index < left.length; index++) {
    difference |= left.charCodeAt(index) ^ right.charCodeAt(index);
  }
  return difference === 0;
}

function uuid_() {
  return Utilities.getUuid().replace(/-/g, '');
}

function json_(result) {
  return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
}
