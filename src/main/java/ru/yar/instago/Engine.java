package ru.yar.instago;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.App;
import ru.yar.instago.like.Like;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Engine {

    private PrimarySettings ps;
    private ChromeDriver chrome;
    private Map<String, String> xpaths;
    private Map<String, String> url;

    private final int humanImitation = 3;
    private final int basic = 1;
    private final Random random;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Engine(String login, String pswrd, String searchWord, long likeCount) {
        LOG.trace("Start the program");
        random = new Random();
        ps = new PrimarySettings(login, pswrd, searchWord, likeCount);
        browserSetUp();
        urlInit();
        xpathsInit();
        login();
        new Like(this, chrome);
    }


    public Engine() {
        LOG.trace("Start the program");
        random = new Random();
        ps = new PrimarySettings();
        browserSetUp();
        urlInit();
        xpathsInit();
        login();
        new Like(this, chrome);

    }


    /**
     * Setups for browser (chrome)
     */
    private void browserSetUp() {
        String path = App.controller.dir.getPath() + "/chromedriver.exe";
        InputStream is;
        BufferedInputStream bis;
        BufferedOutputStream bos;
        try {
            is = Engine.class.getResourceAsStream("/chromedriver.exe");
            bis = new BufferedInputStream(is);
            File chromedriver = new File(path);
            if (!chromedriver.exists()) {
                sendLogMsg("Installing chromedriver.exe...");
                bos = new BufferedOutputStream(new FileOutputStream(chromedriver));
                int bsize = 2048;
                int n = 0;

                byte[] buffer = new byte[bsize];

                while ((n = bis.read(buffer, 0, bsize)) != -1) {
                    bos.write(buffer, 0, n);
                }

                bos.flush();
                try {
                    bis.close();
                    bos.close();
                    is.close();
                } catch (IOException e) {
                    LOG.error("Unable to close streams");
                }
                sendLogMsg("Install chromedriver.exe complete.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Unable to install chromedriver");
        }

        System.setProperty("webdriver.chrome.driver", path);
        chrome = new ChromeDriver();
        chrome.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    /**
     * Login to instagram
     */
    private void login() {
        try {
            chrome.get(url.get("login"));
            waitExactly(3);
            chrome.findElementByName("username").sendKeys(ps.getLogin());
            waitExactly(3);
            chrome.findElementByName("password").sendKeys(ps.getPassword());
            waitExactly(3);
            chrome.findElementByXPath(xpaths.get("login_button")).click();
            waitExactly(5);

            chrome.findElement(By.id("slfErrorAlert"));
            sendLogMsg("Login - failed.");
            chrome.close();
            Thread.currentThread().stop();

        } catch (NoSuchElementException e) {
            firstPopupDisable();
            sendLogMsg("Login - succeed.");
        } catch (ElementClickInterceptedException e) {
            sendLogMsg("Login - failed.");
            chrome.close();
            Thread.currentThread().stop();
        }
    }

    /**
     * This method disable popup window which appears after login
     */
    private void firstPopupDisable() {
        try {
            chrome.findElementByXPath(xpaths.get("not_now_button")).click();
            LOG.trace("Popup window closed");
        } catch (NoSuchElementException e) {
            LOG.trace("Popup window after login does not appear");
        }
    }

    /**
     * Initialize url map
     */
    private void urlInit() {
        url = new HashMap<>();
        url.put("login", "https://www.instagram.com/accounts/login/");
        url.put("instagram", "https://www.instagram.com/");
        url.put("blank", "window.open('about:blank','_blank');");
    }

    /**
     * Initialize xpaths to get the WebElements
     */
    private void xpathsInit() {
        xpaths = new HashMap<>();
        xpaths.put("login_button", "//*[@id=\"loginForm\"]/div/div[3]/button/div");
        xpaths.put("not_now_button", "/html/body/div[4]/div/div/div/div[3]/button[2]");
    }

    /**
     * @param seconds seconds to wait
     */
    public void waitExactly(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            LOG.error("InterruptedException waitExactly" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Imitate human actions by waiting some random time from 0 to int humanImitation
     */
    public void humanImitation() {
        try {
            Thread.sleep((basic + random.nextInt(humanImitation)) * 1000);
        } catch (InterruptedException e) {
            LOG.error("InterruptedException humanImitation" + Arrays.toString(e.getStackTrace()));
        }
    }

    public Map<String, String> getXpaths() {
        return xpaths;
    }

    public Map<String, String> getUrl() {
        return url;
    }

    public PrimarySettings getPs() {
        return ps;
    }

    public void sendLogMsg(String msg) {
        Platform.runLater(() -> App.controller.guiLOG.appendText(new Date() + ": " + msg + "\r\n"));
    }
}
