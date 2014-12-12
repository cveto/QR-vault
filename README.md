QR-vault
========
Android project I built for my personal use of encrypting information and printing it out.
It is my first Android project and first GitHub upload, hopefully someone will like the Idea and help me optimize and check the code securely. Any type of comments and criticism welcome.

Build with: Android Studio Beta 0.8.14
Works with: Android devices API 14 (API 19 for printing support).

How to use: Encrypt
1. Enter a message
2. Enter a (good) password
3. Click Encrypt.
App creates an AES key with PBKDF2 and encrypts the message.
Encrypted message is interpreted with Base-64 and put in QR code (with the help of ZXING) using UTF-8.

How to use: Decrypt
1. Scan the QR code with the app.
2. Enter password
3. Read the message.

Further work (allready done, not uploaded yet)
- Encrypting printable messages using Public-key cryptography.
- Storing RSA certificate and a private key in a QR code.
