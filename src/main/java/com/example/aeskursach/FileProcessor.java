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
        byte[] fileData = readFile(file);
        int partSize = 1024 * 1024; // 1 MB

        List<Future<byte[]>> futures = new ArrayList<>();

        // Разбиваем данные на части и запускаем обработку в нескольких потоках
        for (int i = 0; i < fileData.length; i += partSize) {
            int end = Math.min(fileData.length, i + partSize);
            byte[] part = new byte[end - i];
            System.arraycopy(fileData, i, part, 0, end - i);

            Callable<byte[]> task = () -> {
                if (isEncryption) {
                    return aesUtil.encrypt(part, key, iv, algorithm);
                } else {
                    return aesUtil.decrypt(part, key, iv, algorithm);
                }
            };
            futures.add(executor.submit(task));
        }

        // Собираем результаты
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
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
