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
    private Properties prop;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/prop.properties"));
            if (!prop.getProperty("login").equals("")
                    && !prop.getProperty("password").equals("")
                    && !prop.getProperty("mainSearchWord").equals("")) {
                loginField.setText(prop.getProperty("login"));
                passwordField.setText(prop.getProperty("password"));
                searchWordField.setText(prop.getProperty("mainSearchWord"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(ActionEvent event) {
        if (checkAll()) {
            new Thread(() -> new Engine(loginField.getText(),
                    passwordField.getText(),
                    searchWordField.getText(),
                    Long.parseLong(likesCount.getText()))).start();
        } else {
            guiLOG.appendText("Неверные данные для входа!\r\n" );
        }
    }

    public void stop(ActionEvent event) {
    }

    public void info(ActionEvent event) {

    }

    private boolean checkAll() {
        return !passwordField.getText().equals("")
                && !loginField.getText().equals("")
                && !searchWordField.getText().equals("")
                && !likesCount.getText().equals("");
    }

    public void delete(ActionEvent event) {
        prop.setProperty("login", "");
        prop.setProperty("password", "");
        prop.setProperty("mainSearchWord", "");

        File file = new File("src/main/resources/prop.properties");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            prop.store(fos, "");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(ActionEvent event) {
        if (!passwordField.getText().equals("")
                && !loginField.getText().equals("")
                && !searchWordField.getText().equals("")) {

            prop.setProperty("login", loginField.getText());
            prop.setProperty("password", passwordField.getText());
            prop.setProperty("mainSearchWord", searchWordField.getText());

            File file = new File("src/main/resources/prop.properties");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                prop.store(fos, "");
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
