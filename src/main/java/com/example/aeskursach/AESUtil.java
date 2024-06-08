package com.example.aeskursach;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";

    public SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static SecretKey getKeyFromPassword(String password, String salt) throws Exception {
        byte[] key = (password + salt).getBytes();
        return new SecretKeySpec(key, 0, 16, ALGORITHM);
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public byte[] encrypt(byte[] input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(input);
    }

    public byte[] decrypt(byte[] input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(input);
    }

    public String encrypt(String input, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        byte[] cipherText = encrypt(input.getBytes(), key, iv, algorithm);
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decrypt(String cipherText, SecretKey key, AlgorithmParameterSpec iv, String algorithm) throws Exception {
        byte[] plainText = decrypt(Base64.getDecoder().decode(cipherText), key, iv, algorithm);
        return new String(plainText);
    }

    public static IvParameterSpec getIvFromFile(File file) throws IOException {
        byte[] iv = new byte[16];
        try (InputStream inputStream = new FileInputStream(file)) {
            if (inputStream.read(iv) != 16) {
                throw new IOException("Failed to read IV from file");
            }
        }
        return new IvParameterSpec(iv);
    }
}


