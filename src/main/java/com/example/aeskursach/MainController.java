package com.example.aeskursach;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;

public class MainController {

    @FXML
    private TextField keyField;

    @FXML
    private TextField algorithmField;

    @FXML
    private TextArea outputArea;

    @FXML
    private Button generateKeyButton;

    @FXML
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    private FileProcessor fileProcessor;

    @FXML
    public void initialize() {
        fileProcessor = new FileProcessor(4); // Инициализация FileProcessor с 4 потоками
    }

    @FXML
    private void onGenerateAndSaveKey() {
        try {
            // Генерация нового ключа
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();

            // Сохранение ключа в файл
            SecureKeyStorage.saveSecretKey(secretKey);

            outputArea.setText("Key generated and saved successfully.");
        } catch (Exception e) {
            outputArea.setText("Error generating/saving key: " + e.getMessage());
        }
    }

    @FXML
    private void onEncrypt() {
        processFile(true);
    }

    @FXML
    private void onDecrypt() {
        processFile(false);
    }

    private void processFile(boolean isEncryption) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                String algorithm = algorithmField.getText().trim();
                if (algorithm.isEmpty()) {
                    algorithm = "AES/CBC/PKCS5Padding"; // Значение по умолчанию
                }

                // Проверка наличия файла ключа, создание и сохранение ключа, если файла нет
                File keyFile = new File(SecureKeyStorage.KEY_FILE);
                if (!keyFile.exists()) {
                    onGenerateAndSaveKey();
                }

                SecretKey key = SecureKeyStorage.loadSecretKey();
                IvParameterSpec iv = null;

                if (!algorithm.contains("ECB")) {
                    if (isEncryption) {
                        iv = AESUtil.generateIv();
                        // Сохранение IV в отдельный файл
                        AESUtil.saveIvToFile(iv, new File(file.getParent(), file.getName() + ".iv"));
                    } else {
                        // Извлечение IV из отдельного файла
                        iv = AESUtil.getIvFromFile(new File(file.getParent(), file.getName() + ".iv"));
                    }
                }

                fileProcessor.processFile(file, isEncryption, new AESUtil(), algorithm, key, iv);

                outputArea.setText("Operation completed on file: " + file.getName());
            } catch (Exception e) {
                outputArea.setText("Error: " + e.getMessage());
            }
        }
    }
}

