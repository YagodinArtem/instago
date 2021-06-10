package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Engine {

    private PrimarySettings ps;
    private ChromeDriver chrome;
    private Map<String, String> xpaths;
    private Map<String, String> url;

    private final int humanImitation = 7;
    private final int basic = 2;
    private final Random random;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

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
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        chrome = new ChromeDriver();
        chrome.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        //chrome.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * Login to instagram
     */
    private void login() {
        chrome.get(url.get("login"));
        waitExactly(3);
        chrome.findElementByName("username").sendKeys(ps.getLogin());
        waitExactly(3);
        chrome.findElementByName("password").sendKeys(ps.getPassword());
        waitExactly(3);
        chrome.findElementByXPath(xpaths.get("login_button")).click();
        waitExactly(3);
        LOG.trace("Login succeed");
        firstPopupDisable();
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
        xpaths.put("search_after_login", "//*[@id=\"react-root\"]/section/nav/div[2]/div/div/div[2]/input");
        xpaths.put("followers", "//*[@id=\"react-root\"]/section/main/div/header/section/ul/li[2]/a");

        xpaths.put("first_photo", "//*[@id=\"react-root\"]/section/main/div/div[3]/article/div/div/div[1]/div[1]");
        xpaths.put("like_button", "/html/body/div[5]/div[2]/div/article/div[3]/section[1]/span[1]");

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
}
