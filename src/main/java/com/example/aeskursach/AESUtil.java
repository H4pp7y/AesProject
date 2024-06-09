package com.example.aeskursach;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH = 16 * 8; // 16 bytes

    public SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static SecretKey getKeyFromPassword(String password, String salt) {
        byte[] key = (password + salt).getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(key, 0, 16, ALGORITHM);
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public byte[] encrypt(byte[] input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        if (algorithm.contains("GCM")) {
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, ((IvParameterSpec) iv).getIV()));
        } else if (algorithm.contains("ECB")) {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        }
        return cipher.doFinal(input);
    }

    public byte[] decrypt(byte[] input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        if (algorithm.contains("GCM")) {
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, ((IvParameterSpec) iv).getIV()));
        } else if (algorithm.contains("ECB")) {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
        }
        return cipher.doFinal(input);
    }

    public String encrypt(String input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = encrypt(inputBytes, key, iv, algorithm);
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decrypt(String cipherText, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
        byte[] plainText = decrypt(cipherBytes, key, iv, algorithm);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    // Метод для сохранения IV в файл
    public static void saveIvToFile(IvParameterSpec iv, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(iv.getIV());
        }
    }

    // Метод для извлечения IV из файла
    public static IvParameterSpec getIvFromFile(File file) throws IOException {
        byte[] iv = new byte[16];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(iv);
        }
        return new IvParameterSpec(iv);
    }
}
