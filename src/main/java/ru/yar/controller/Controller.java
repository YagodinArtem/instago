package ru.yar.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.yar.instago.Engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public TextField passwordField;
    public TextField loginField;
    public TextField searchWordField;
    public TextField likesCount;
    public TextArea guiLOG;
    public TextArea userMessage;
    private Properties prop;
    private File file;
    public File dir;
    private String filePath;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prop = new Properties();
        dir = new File(System.getProperty("user.home") + "/" + "instago");
        filePath = System.getProperty("user.home") + "/" + dir.getName() + "/prop.properties";
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            file = new File(filePath);

            if (!file.exists()) {
                file.createNewFile();
                prop.put("login", "");
                prop.put("password", "");
                prop.put("mainSearchWord", "");
                prop.put("message", "");
                storeProp();
            } else {
                prop.load(new FileInputStream(filePath));
            }
            if (!prop.getProperty("login").equals("")
                    && !prop.getProperty("password").equals("")) {
                loginField.setText(prop.getProperty("login"));
                passwordField.setText(prop.getProperty("password"));
                searchWordField.setText(prop.getProperty("mainSearchWord"));
                userMessage.setText(prop.getProperty("message"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startLike(ActionEvent event) {
        if (checkAll()) {
            new Thread(() -> new Engine(loginField.getText(),
                    passwordField.getText(),
                    searchWordField.getText(),
                    Long.parseLong(likesCount.getText()))).start();
        } else {
            guiLOG.appendText("Неверные данные для входа!\r\n");
        }
    }

    public void startMessage(ActionEvent event) {
        if (checkLoginPswAndMessage()) {
            new Thread(() -> new Engine(loginField.getText(),
                    passwordField.getText(), userMessage.getText())).start();
        } else {
            guiLOG.appendText("Неверные данные для входа!\r\n");
        }
    }

    public void stop(ActionEvent event) {
        System.exit(0);
    }

    public void info(ActionEvent event) {

    }

    public void save(ActionEvent event) {
        if (!passwordField.getText().equals("")
                && !loginField.getText().equals("")) {

            prop.setProperty("login", loginField.getText());
            prop.setProperty("password", passwordField.getText());
            prop.setProperty("mainSearchWord", searchWordField.getText());
            prop.setProperty("message", userMessage.getText());

            storeProp();
        }
    }

    public void delete(ActionEvent event) {
        prop.setProperty("login", "");
        prop.setProperty("password", "");
        prop.setProperty("mainSearchWord", "");

        storeProp();
    }

    private boolean checkLoginPswAndMessage() {
        return !passwordField.getText().equals("")
                && !loginField.getText().equals("")
                && !userMessage.getText().equals("");
    }

    private boolean checkAll() {
        return !passwordField.getText().equals("")
                && !loginField.getText().equals("")
                && !searchWordField.getText().equals("")
                && !likesCount.getText().equals("");
    }

    private void storeProp() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            prop.store(fos, "");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
