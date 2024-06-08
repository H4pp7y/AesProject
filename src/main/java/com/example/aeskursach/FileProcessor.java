package com.example.aeskursach;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileProcessor {

    private ExecutorService executor;

    public FileProcessor(int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public void processFile(File file, boolean isEncryption, AESUtil aesUtil, String algorithm, SecretKey key, IvParameterSpec iv) {
        executor.submit(() -> {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String result;
                if (isEncryption) {
                    result = AESUtil.encrypt(algorithm, new String(fileContent), key, iv);
                } else {
                    result = AESUtil.decrypt(algorithm, new String(fileContent), key, iv);
                }
                Files.write(file.toPath(), result.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}