const crypto = require('crypto');
const fs = require('fs');

const username = 'weather';
const password = 'W3ather@P@ssw0rd!2026#';

const hash = crypto.createHash('md5').update(`${username}:${password}`).digest('base64');

const htpasswdContent = `${username}:${hash}\n`;

fs.writeFileSync('D:/nginx-1.26.3/conf/.htpasswd', htpasswdContent);
console.log('.htpasswd file created successfully');
console.log('Username:', username);
console.log('Password:', password);