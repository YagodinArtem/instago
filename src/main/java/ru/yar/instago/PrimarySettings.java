package ru.yar.instago;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimarySettings {

    private String login;
    private String password;
    private final String propertiesFileName = "prop.properties";

    private String mainSearchWord;
    private long likeCount;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public PrimarySettings(String login, String password, String mainSearchWord, long likeCount) {
        this.login = login;
        this.password = password;
        this.mainSearchWord = mainSearchWord;
        this.likeCount = likeCount;
    }

    public PrimarySettings() {

        try (FileReader reader = new FileReader(String.format("src/main/resources/%s", propertiesFileName))){
            Properties p = new Properties();
            p.load(reader);
            login = p.getProperty("login");
            password = p.getProperty("password");
            mainSearchWord = p.getProperty("mainSearchWord");
            LOG.trace("Reading properties succeed");
        } catch (IOException e) {
            LOG.error("Unable to read properties");
        }
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getMainSearchWord() {
        return mainSearchWord;
    }

    public long getLikeCount() {
        return likeCount;
    }
}
