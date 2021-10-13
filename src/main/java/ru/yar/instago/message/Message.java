package ru.yar.instago.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.yar.instago.Engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Message {

    private Engine engine;
    private ChromeDriver chrome;
    private Set<String> links;
    private ArrayList<String> tabs;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    /**
     * Количество сообщение к отправке
     */
    private int messageCount;


    public Message(Engine engine, ChromeDriver chrome, int messageCount) {
        this.engine = engine;
        this.chrome = chrome;
        links = new HashSet<>();
        this.messageCount = messageCount;
        start();
        findFirst();
    }

    /**
     * Открыть вкладку Москва
     */
    private void start() {
        createNewTab();
        createNewTab();
        chrome.get(engine.getUrl().get("moscow"));
    }

    /**
     * находит и открывает первую публикацию на странице
     * @return true если удалось открыть публикацию
     */
    private boolean findFirst() {
        String cName = "_9AhH0";
        try {
            chrome.findElement(By.className(cName)).click();
            return true;
        } catch (NoSuchElementException e) {
            LOG.trace("Classname не найден - " + cName);
            return false;
        } catch (ElementNotInteractableException e) {
            LOG.trace("Элемент не интерактивен - " + cName);
            return false;
        } catch (StaleElementReferenceException e) {
            LOG.trace("Элемент не прикреплен к странице");
            return false;
        }
    }

    /**
     * Открыть новую вкладку
     */
    private void createNewTab() {
        ((JavascriptExecutor) chrome).executeScript(engine.getUrl().get("blank"));
        tabs = new ArrayList<>(chrome.getWindowHandles());
        chrome.switchTo().window(tabs.get(0));
    }
}
