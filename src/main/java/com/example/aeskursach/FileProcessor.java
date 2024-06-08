package com.example.aeskursach;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileProcessor {

    private final ExecutorService executor;

    public FileProcessor(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void processFile(File file, boolean isEncryption, AESUtil aesUtil, String algorithm, SecretKey key, IvParameterSpec iv) throws Exception {
        byte[] fileData;
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        if (isEncryption) {
            fileData = readFile(file);
            resultStream.write(iv.getIV());
        } else {
            // При расшифровании читаем IV из файла
            byte[] fileContent = readFile(file);
            byte[] ivBytes = new byte[16];
            System.arraycopy(fileContent, 0, ivBytes, 0, 16);
            iv = new IvParameterSpec(ivBytes);
            fileData = new byte[fileContent.length - 16];
            System.arraycopy(fileContent, 16, fileData, 0, fileData.length);
        }

        int blockSize = 16; // Размер блока для шифрования AES
        List<Future<byte[]>> futures = new ArrayList<>();

        for (int i = 0; i < fileData.length; i += blockSize) {
            int end = Math.min(fileData.length, i + blockSize);
            byte[] block = new byte[end - i];
            System.arraycopy(fileData, i, block, 0, end - i);

            IvParameterSpec finalIv = iv;
            Callable<byte[]> task = () -> {
                if (isEncryption) {
                    return aesUtil.encrypt(block, key, finalIv, algorithm);
                } else {
                    return aesUtil.decrypt(block, key, finalIv, algorithm);
                }
            };
            futures.add(executor.submit(task));
        }

        for (Future<byte[]> future : futures) {
            resultStream.write(future.get());
        }

        writeFile(file, resultStream.toByteArray());
    }

    private byte[] readFile(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return inputStream.readAllBytes();
        }
    }

    private void writeFile(File file, byte[] data) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
