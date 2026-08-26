const crypto = require('crypto');
const password = 'W3ather@P@ssw0rd!2026#';
const hash = crypto.createHash('sha256').update(password).digest('hex');
console.log('Password:', password);
console.log('Hash:', hash);