package ru.yar.instago;

import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.NewSessionPayload;
import ru.App;
import ru.yar.controller.Controller;
import ru.yar.instago.like.Like;
import ru.yar.instago.message.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ru.yar.controller.Controller.dir;

public class Engine {

    private static final String CHROMEDRIVER = "/chromedriver.exe";
    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    private PrimarySettings ps;
    public static ChromeDriver chrome;
    private Map<String, String> xpaths;
    private Map<String, String> url;

    private final int humanImitation = 3;
    private final int basic = 1;
    private final Random random;

    private boolean updateDriver;
    private boolean shadowMode;
    private String driverVersion;


    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Engine(String login,
                  String pswrd,
                  String searchWord,
                  long likeCount,
                  boolean shadowMode,
                  boolean updateDriver,
                  String driverVersion) {
        sendLogMsg("Start liking");
        random = new Random();
        ps = new PrimarySettings(login, pswrd, searchWord, likeCount);
        this.shadowMode = shadowMode;
        this.updateDriver = updateDriver;
        this.driverVersion = driverVersion;
        browserSetUp(shadowMode);
        urlInit();
        xpathsInit();
        login();
        new Like(this, chrome);
    }

    //TODO same constructors
    public Engine(String login, String pswrd, String message, int messageCount, boolean shadowMode) {
        sendLogMsg("Start messaging");
        random = new Random();
        ps = new PrimarySettings(login, pswrd, message);
        browserSetUp(shadowMode);
        urlInit();
        xpathsInit();
        login();
        new Message(this, chrome, messageCount);
    }


    /**
     * Setups for browser (chrome)
     */
    private void browserSetUp(boolean shadowMode) {
        if (shadowMode) {
            sendLogMsg("""
                    Don`t worry! You chose work in shadow mode,\s
                    and you will not see browser, but it works, watch the log window :D\s
                    next time if you want to see browser at work, just don`t pass the check box""");
        }
        String path = dir.getPath() + CHROMEDRIVER;

        installChromeDriver(path);

        System.setProperty(WEBDRIVER_CHROME_DRIVER, path);
        Capabilities capabilities = null;
        ChromeDriver testDriver = null;
        String currentDriverVersion;
        String currentBrowserVersion;
        try {
            testDriver = getChromeWithOptions(shadowMode);
            capabilities = testDriver.getCapabilities();
            LOG.info(capabilities.getVersion() + "RAW 102");

            Map<String, String> a = (Map<String, String>) capabilities.getCapability("chrome");
            currentBrowserVersion = chromeVersion(); //get current browser version

            sendLogMsg("Current browser version: " + currentBrowserVersion);
            sendLogMsg("Current driver version: " + a.get("chromedriverVersion"));

            if (updateDriver) {
                downloadAndInstallNewDriverVersion(path);
            } else {
                chrome = getChromeWithOptions(shadowMode);
            }

        } catch (Exception e) {
            LOG.info(e.getMessage());
            e.printStackTrace();
            downloadAndInstallNewDriverVersion(path);
        } finally {
            if (testDriver != null) {
                testDriver.quit();
            }
        }

        chrome.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    private void downloadAndInstallNewDriverVersion(String path) {
        try {
            if (new File(path).delete()) {
                sendLogMsg("Old driver deleted");
            }
            killChromedriverExeService();

            sendLogMsg("Start downloading new chromedriver.exe");
            URL driverDownloadUrl =
                    new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip",
                            driverVersion));
            sendLogMsg(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip",
                    driverVersion));

            File downloadedDriverInZip = new File(Controller.dir.getPath() + "/chromedriver_win32");
            FileUtils.copyURLToFile(driverDownloadUrl, downloadedDriverInZip);

            unzipDriver(downloadedDriverInZip);

            System.setProperty("webdriver.chrome.driver", path);
            chrome = getChromeWithOptions(shadowMode);
            sendLogMsg("Restart the application");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendLogMsg("New chromedriver.exe was successfully installed in: " + path);
        }
    }

    private ChromeDriver getChromeWithOptions(boolean shadowMode) {
        ChromeOptions options = new ChromeOptions();
        if (shadowMode) {
            options.addArguments("headless");
        } else {
            options.addExtensions(new File("C:\\Users\\root\\Desktop\\vpn.zip"));
        }
        return new ChromeDriver(options);
    }

    private String chromeVersion() {

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("reg query " + "HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon " + "/v version");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("version") || s.contains("REG_SZ")) {

                    StringBuilder sb = new StringBuilder();
                    for (Character c : s.toCharArray()) {
                        if (Character.isDigit(c) || c == '.') {
                            sb.append(c);
                        }
                    }

                    if (sb.toString().length() > 0) {
                        return sb.toString();
                    }
                }
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                LOG.error(s);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        sendLogMsg("Chrome driver version not founded =(");
        return "";
    }

    public static void killChromedriverExeService() throws IOException {
        Runtime.getRuntime().exec(String.format("taskkill /F /IM %s", "chromedriver.exe"));
    }

    private void unzipDriver(File downloadedDriverInZip) {
        byte[] buffer = new byte[1024];
        ZipEntry zipEntry;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadedDriverInZip));) {
            zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(Controller.dir.getPath() + CHROMEDRIVER);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDriverVersion(Capabilities all) {
        String chromeCapabilities = all.getCapability("chrome").toString().split(" ")[0];
        chromeCapabilities = chromeCapabilities.replaceFirst("[{]", "");
        return chromeCapabilities.split("=")[1];
    }

    private void installChromeDriver(String path) {
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
    }


    /**
     * Login to instagram
     */
    private void login() {
        try {
            chrome.get(url.get("login"));
            waitExactly(3);
            chrome.findElement(By.name("username")).sendKeys(ps.getLogin());
            waitExactly(3);
            chrome.findElement(By.name("password")).sendKeys(ps.getPassword());
            waitExactly(3);
            chrome.findElement(By.xpath((xpaths.get("login_button")))).click();
            waitExactly(7);

            chrome.findElement(By.id("slfErrorAlert"));
            waitExactly(3);
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
            chrome.findElement(By.xpath(xpaths.get("not_now_button"))).click();
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
        url.put("moscow", "https://www.instagram.com/explore/locations/108889350634961/moscow/");
    }

    /**
     * Initialize xpaths to get the WebElements
     */
    private void xpathsInit() {
        xpaths = new HashMap<>();
        xpaths.put("login_button", "//*[@id=\"loginForm\"]/div/div[3]/button");
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
        LOG.info(msg);
        Platform.runLater(() -> App.controller.guiLOG.appendText(new Date() + ": " + msg + "\r\n"));
    }
}
