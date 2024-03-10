package com.ltm.encrypt;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    // Độ dài của key phải là 16, 24 hoặc 32 bytes
    private static final String SECRET_KEY = "duongvanmanh0412";
    private static final String ALGORITHM = "AES";

    public static byte[] encrypt(byte[] data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    private static Key generateKey() throws Exception {
        byte[] keyData = SECRET_KEY.getBytes();
        return new SecretKeySpec(keyData, ALGORITHM);
    }
}

