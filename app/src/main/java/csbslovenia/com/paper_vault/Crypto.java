package csbslovenia.com.paper_vault;


        import java.io.UnsupportedEncodingException;
        import java.security.GeneralSecurityException;
        import java.security.Provider;
        import java.security.Provider.Service;
        import java.security.SecureRandom;
        import java.security.Security;
        import java.security.spec.KeySpec;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Collections;
        import java.util.List;
        import java.util.Set;

        import javax.crypto.Cipher;
        import javax.crypto.KeyGenerator;
        import javax.crypto.SecretKey;
        import javax.crypto.SecretKeyFactory;
        import javax.crypto.spec.IvParameterSpec;
        import javax.crypto.spec.PBEKeySpec;
        import javax.crypto.spec.PBEParameterSpec;
        import javax.crypto.spec.SecretKeySpec;

        import android.util.Base64;
        import android.util.Log;

public class Crypto {

    // What I actually need
    public static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";  // Key derivateion method
    private static int KEY_LENGTH = 256;                                            // Key length is 256 because we will use AES-256
    private static int ITERATION_COUNT = 65535;                                      // Just to slow down the creation of key (so the evil person will spend more money on decrypting) 65535 is biggest int
    private static final int PKCS5_SALT_LENGTH = 8;                                 // Because internet sais so
    private static SecureRandom random = new SecureRandom();                        // All goes down to this random function. Do you trust Google developers? Maybe upgrade this to from random.org.
    private static String DELIMITER = "]";                                          // ] is not part of Byte64. Any character that isnt there would do. We could do an xml file instead, but naaaah.
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";          // This is what we use to encrypt

    private static final String TAG = Crypto.class.getSimpleName();                 // no idea what this is


    public static SecretKey deriveKeyPbkdf2(byte[] salt, char[] password) {
        try {
            long start = System.currentTimeMillis();
            KeySpec keySpec = new PBEKeySpec(password, salt,ITERATION_COUNT, KEY_LENGTH);     // What kind of key do I want?
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_DERIVATION_ALGORITHM);        // How will I make this key?
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();                              // Execute!
            Log.d(TAG, "key bytes: " + toHex(keyBytes));

            SecretKey result = new SecretKeySpec(keyBytes, "AES");                                          // make a SecretKey class from bytes, that AES will like?
            long elapsed = System.currentTimeMillis() - start;
            Log.d(TAG, String.format("PBKDF2 key derivation took %d [ms].",
                    elapsed));

            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);

        return b;
    }

    public static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);                        // random was created at the beginning. Make some random bytes using the random generator.
        return b;
    }

    public static String encrypt(String plaintext, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);                   // How to encrypt? (With some AES standard defined above)
            byte[] iv = generateIv(cipher.getBlockSize());                          // some randomness
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);                     // Make an IvParameterSpec class from the random iv.
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);                        // Initialize Cipher. We will encrypt using key and some randomness.
            Log.d(TAG, "Cipher IV: " + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));        //  Encrypt. Encrypt UTF-8 bytes that you got from the String.

            if (salt != null) {
                return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER, toBase64(iv), DELIMITER, toBase64(cipherText));
                                                                                    // Return chiper text: First salt, then IV, then Cipherthext..
            }

            // Return concencated string,
            return String.format("%s%s%s", toBase64(iv), DELIMITER,toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }


    public static String decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
            byte[] plaintext = cipher.doFinal(cipherBytes);
            String plainrStr = new String(plaintext, "UTF-8");

            return plainrStr;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public static String decryptPbkdf2(String ciphertext, char[] password) {
        String[] fields = ciphertext.split(DELIMITER);


        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }

        byte[] salt = fromBase64(fields[0]);
        byte[] iv = fromBase64(fields[1]);
        byte[] cipherBytes = fromBase64(fields[2]);
        SecretKey key = deriveKeyPbkdf2(salt, password);

        return decrypt(cipherBytes, key, iv);
    }
}
