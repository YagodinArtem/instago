package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
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
    private Set<String> handledLinks;
    private Like like;

    /** dayPermission
     * вероятное допустимое количество лайков в день
     */
    private int dayPermission = 300;

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
     * How many cycles will be performed ON each MAJOR account
     * Определяет какое количество циклов будем производить над аккаунтом ( каждый цикл брабатывает scrollCount * 12 подписчиков)
     * например, если scrollCount = 5 и cycleLimit = 5, 5 раз будет обработано 5 * 12 (60) подписчиков.
     */
    private int cycleLimit = 5;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    private static int likeCount = 0;


    public LikeProcessor(Engine engine, ChromeDriver chrome, Like like) {
        this.like = like;
        this.engine = engine;
        this.chrome = chrome;
        subscribersAccLinks = new HashSet<>();
        handledLinks = new HashSet<>();
    }

    /**
     *
     * @param majorLink на главный аккаунт
     */
    public void startLiking(String majorLink) {
        chrome.switchTo().window(tabs.get(0));
        chrome.get(majorLink);
        boolean isClicked = false;
        try {
            for (WebElement w : chrome.findElements(By.tagName("a"))) {
                if (w.getAttribute("href").contains("followers")
                        && !w.findElement(By.tagName("span")).getAttribute("title").equals("0")) {
                    engine.waitExactly(1);
                    w.click();
                    isClicked = true;
                }
            }
            if (!isClicked) {
                LOG.trace("Не могу найти подписчиков у : " + majorLink);
                throw new NoSuchElementException("Не могу найти подписчиков");
            }
        } catch (NoSuchElementException | NullPointerException e) {
            LOG.trace("majorLink: " + majorLink + " - does not have any subscribers (NoSuchElem | NullPointer");
            return;
        } catch (ElementClickInterceptedException e) {
            LOG.trace("element click intercepted");
        }

        for (int i = 0; i < cycleLimit; i++) {
            scrollDown();
            getSubscribersHrefs();

            for (String subscriber : subscribersAccLinks) {
                if (subscriber.contains("www.instagram.com")) {
                    likeTenSubscriberPhoto(subscriber);
                }
                handledLinks.add(subscriber);
            }
        }

    }

    /**
     * Получает список WebElement по тегу <a> ... </a>
     * внутри которого находит и добавляет в список subscribersAccLinks прямую ссылку на страницу подписчика
     */

    private void getSubscribersHrefs() {
        chrome.switchTo().window(tabs.get(0));
        subscribersAccLinks.clear();
        List<WebElement> aTag = chrome.findElements(By.tagName("a"));
        for (WebElement aClass : aTag) {
            try {
                if (!handledLinks.contains(aClass.getAttribute("href"))) {
                    subscribersAccLinks.add(aClass.getAttribute("href"));
                }
            } catch (StaleElementReferenceException e) {
                LOG.trace("element /href is not attached to the page document");
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
     * Find photo in profile
     *
     * @return true if founded
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
     * Perform like on current photo, if like is set do nothing
     */
    private void likeCurrent() {
        try {
            engine.humanImitation();
            WebElement likeButton = chrome.findElementByXPath(engine.getXpaths().get("like_button"));
            if (!likeButton
                    .findElement(By.tagName("svg"))
                    .getAttribute("fill").equals("#ed4956")) {
                likeButton.click();
                System.out.println(++likeCount);
                if (likeCount > dayPermission) {
                    LOG.trace("Поставили " + dayPermission + " лайков, приложение заввершает работу");
                    like.closeAll();
                    System.exit(0);
                }
            }
        } catch (NoSuchElementException e) {
            LOG.error("Не могу найти кнопку лайк! Возможно не загрузилась страница");
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
        engine.waitExactly(2);
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
