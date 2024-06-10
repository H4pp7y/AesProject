package com.example.aeskursach;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    @FXML
    private Button loadFileButton;

    @FXML
    private Button saveFileButton;

    private FileProcessor fileProcessor;
    private File selectedFile;

    @FXML
    public void initialize() {
        fileProcessor = new FileProcessor(4); // Инициализация FileProcessor с 4 потоками
        setupButtons();
    }

    private void setupButtons() {
        generateKeyButton.setOnAction(event -> onGenerateAndSaveKey());
        encryptButton.setOnAction(event -> onEncrypt());
        decryptButton.setOnAction(event -> onDecrypt());
        loadFileButton.setOnAction(event -> onLoadFile());
        saveFileButton.setOnAction(event -> onSaveFile());
    }

    private void onLoadFile() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            outputArea.setText("File loaded: " + selectedFile.getName());
        } else {
            outputArea.setText("File loading cancelled.");
        }
    }

    private void onSaveFile() {
        if (selectedFile != null) {
            FileChooser fileChooser = new FileChooser();
            File saveFile = fileChooser.showSaveDialog(new Stage());

            if (saveFile != null) {
                try {
                    byte[] fileData = AESUtil.readFile(selectedFile);
                    AESUtil.writeFile(saveFile, fileData);
                    outputArea.setText("File saved: " + saveFile.getName());
                } catch (Exception e) {
                    outputArea.setText("Error saving file: " + e.getMessage());
                }
            } else {
                outputArea.setText("File saving cancelled.");
            }
        } else {
            outputArea.setText("No file loaded to save.");
        }
    }

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
    private void onChooseFile() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            outputArea.setText("Selected file: " + selectedFile.getName());
        } else {
            outputArea.setText("No file selected.");
        }
    }

    @FXML
    private void onEncrypt() {
        if (selectedFile != null) {
            processFile(true);
        } else {
            outputArea.setText("No file selected.");
        }
    }

    @FXML
    private void onDecrypt() {
        if (selectedFile != null) {
            processFile(false);
        } else {
            outputArea.setText("No file selected.");
        }
    }

    private void processFile(boolean isEncryption) {
        if (selectedFile != null) {
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
                IvParameterSpec iv;

                if (isEncryption) {
                    iv = AESUtil.generateIv();
                    // Сохранение IV в отдельный файл
                    AESUtil.saveIvToFile(iv, new File(selectedFile.getParent(), selectedFile.getName() + ".iv"));
                } else {
                    // Извлечение IV из отдельного файла
                    iv = AESUtil.getIvFromFile(new File(selectedFile.getParent(), selectedFile.getName() + ".iv"));
                }

                fileProcessor.processFile(selectedFile, isEncryption, new AESUtil(), algorithm, key, iv);

                if (isEncryption) {
                    outputArea.setText("File encrypted successfully: " + selectedFile.getName());
                } else {
                    outputArea.setText("File decrypted successfully: " + selectedFile.getName());
                }
            } catch (Exception e) {
                outputArea.setText("Error: " + e.getMessage());
            }
        } else {
            outputArea.setText("No file selected.");
        }
    }
}
