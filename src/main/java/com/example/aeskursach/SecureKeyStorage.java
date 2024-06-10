package com.example.aeskursach;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureKeyStorage {

    // Константы настройки
    private static final String ALGORITHM = "AES"; // Алгоритм шифрования
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // Режим работы и заполнения
    public static final String KEY_FILE = "secretKey.enc"; // Файл для хранения зашифрованного ключа
    private static final String MASTER_KEY = "MyMasterKey12345"; // Мастер-ключ для шифрования и дешифрования ключей


    // Шифрование и сохранение ключа
    public static void saveSecretKey(SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey masterKey = getKeyFromPassword(MASTER_KEY);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        byte[] encryptedKey = cipher.doFinal(secretKey.getEncoded());

        try (FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
            fos.write(encryptedKey);
        }
    }

    // Загрузка и дешифрование ключа
    public static SecretKey loadSecretKey() throws Exception {
        File file = new File(KEY_FILE);
        byte[] encryptedKey = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(encryptedKey);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey masterKey = getKeyFromPassword(MASTER_KEY);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedKey = cipher.doFinal(encryptedKey);

        return new SecretKeySpec(decryptedKey, ALGORITHM);
    }

    // Генерация мастер-ключа из пароля
    private static SecretKey getKeyFromPassword(String password) {
        byte[] key = password.getBytes();
        return new SecretKeySpec(key, 0, 16, ALGORITHM);
    }
}
