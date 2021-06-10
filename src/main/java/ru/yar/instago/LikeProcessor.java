package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LikeProcessor {

    private Engine engine;
    private ChromeDriver chrome;
    private ArrayList<String> tabs;
    private Set<String> subscribersAccLinks;

    /** photosToLike
     * How many photos will be liked
     * Сколько фоток будем лайкать
     */
    private int photosToLike = 10;

    /**
     * scrollCount
     * Количество раз, сколько будем скроллить область поиска подписчиков
     * (чем больше скроллов тем больше подгрузится подписчиков, одна условная единица подгружает 12 подписчиков)
     */
    private int scrollCount = 1;

    /** cycleLimit
     * How many cycles will be performed ON each MAJOR account ( each cycle about 300-320 subscribers )
     * Определяет какое количество циклов будем производить над аккаунтом ( каждый цикл брабатывает около 300-320 подписчиков)
     */
    private int cycleLimit = 5;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());


    public LikeProcessor(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        subscribersAccLinks = new HashSet<>();
    }

    /**
     *
     * @param majorLink на главный аккаунт
     */
    public void startLiking(String majorLink) {
        chrome.get(majorLink);
        try {
            chrome.findElementByXPath(engine.getXpaths().get("followers")).click();
        } catch (NoSuchElementException e) {
            LOG.trace("majorLink: " + majorLink + " - does not have any subscribers");
            return;
        }

        for (int i = 0; i < cycleLimit; i++) {
            scrollDown();
            getSubscribersHrefs();

            for (String subscriber : subscribersAccLinks) {
                if (subscriber.contains("www.instagram.com"))
                likeTenSubscriberPhoto(subscriber);
            }
        }

    }

    /**
     * Получает список WebElement по тегу <a> ... </a>
     * внутри которого находит и добавляет в список subscribersAccLinks прямую ссылку на страницу подписчика
     */
    //TODO сам себя удаляет! реализовать через два списка
    private void getSubscribersHrefs() {
        chrome.switchTo().window(tabs.get(0));
        List<WebElement> aTag = chrome.findElements(By.tagName("a"));
        for (WebElement aClass : aTag) {
            if (subscribersAccLinks.contains(aClass.getAttribute("href"))) {
                subscribersAccLinks.remove(aClass.getAttribute("href"));
            } else {
                subscribersAccLinks.add(aClass.getAttribute("href"));
            }
        }
        LOG.trace("Добавлено " + subscribersAccLinks.size() + " ссылок подписчиков");
    }

    /**
     * Perform ten likes
     *
     * @param link on subscriber page
     */
    private void likeTenSubscriberPhoto(String link) {
        int count = 0;
        chrome.switchTo().window(tabs.get(1));
        chrome.get(link);

        if (findFirst()) {
            likeCurrent();
            count++;
            while (getNext() && count < photosToLike) {
                likeCurrent();
                count++;
            }
        }
    }

    /**
     * Find first photo in profile
     *
     * @return true if founded
     */
    private boolean findFirst() {
        try {
            chrome.findElementByXPath(engine.getXpaths().get("first_photo")).click();
            return true;
        } catch (NoSuchElementException ex) {
            LOG.trace("Unable to find any photos");
            return false;
        }
    }

    /**
     * Perform like on current photo, if like is set do nothing
     */
    private void likeCurrent() {
        try {
            WebElement likeButton = chrome.findElementByXPath(engine.getXpaths().get("like_button"));
            if (!likeButton
                    .findElement(By.tagName("svg"))
                    .getAttribute("fill").equals("#ed4956")) {
                likeButton.click();
            }
        } catch (NoSuchElementException e) {
            LOG.error("Не могу найти кнопку лайк! Страница не грузится");
        }
    }

    /**
     * @return true если нашел следующую фотку
     */
    private boolean getNext() {
        try {
            chrome.findElement(By.className("coreSpriteRightPaginationArrow")).click();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Циклично вызывает прокручивание диалогового окна поиска подписчиков
     */
    private void scrollDown() {
        int count = 0;
        chrome.switchTo().window(tabs.get(0));
        while (((JavascriptExecutor) chrome).executeScript("return document.readyState").equals("complete")) {
            scrollDownOnce();
            count++;
            if (count > scrollCount) return;
        }

    }

    /**
     * Прокрутить единожды
     */
    private void scrollDownOnce() {
        engine.waitExactly(1);
        ((JavascriptExecutor) chrome)
                .executeScript("arguments[0].scrollBy(0,500)",
                        chrome.findElement(By.className("isgrP")));
    }

    /**
     * Открыть новую вкладку
     */
    public void createNewTab() {
        ((JavascriptExecutor) chrome).executeScript(engine.getUrl().get("blank"));
        tabs = new ArrayList<>(chrome.getWindowHandles());
        chrome.switchTo().window(tabs.get(0));
    }
}
