package com.example.aeskursach;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

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
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    private FileProcessor fileProcessor;

    @FXML
    public void initialize() {
        fileProcessor = new FileProcessor(4);
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
                String keyStr = keyField.getText();
                String algorithm = algorithmField.getText();
                SecretKey key = AESUtil.getKeyFromPassword(keyStr, "12345678");
                IvParameterSpec iv = AESUtil.generateIv();
                fileProcessor.processFile(file, isEncryption, new AESUtil(), algorithm, key, iv);
                outputArea.setText("Operation completed on file: " + file.getName());
            } catch (Exception e) {
                outputArea.setText("Error: " + e.getMessage());
            }
        }
    }
}


