package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;


public class LikeProcessor {

    private Engine engine;
    private ChromeDriver chrome;
    private ArrayList<String> tabs;

    private int subscribersHandleLimit = 1000;
    private int photosToLike = 10;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public LikeProcessor(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
    }

    public void createNewTab() {
        engine.waitExactly(1);
        ((JavascriptExecutor) chrome).executeScript(engine.getUrl().get("blank"));
        engine.waitExactly(1);
        tabs = new ArrayList<>(chrome.getWindowHandles());
        chrome.switchTo().window(tabs.get(0));
    }


    //TODO now it works, but we have only 12-13 subsc because we
    // need to perform click on arrow to dynamically load up a next part of subsc
    public String startLiking(String majorLink) {
        int handledSubscribersCount = 0;
        StringBuilder link = new StringBuilder();
        WebElement temp;

        chrome.get(majorLink);
        engine.waitExactly(3);
        try {
            chrome.findElementByXPath(engine.getXpaths().get("followers")).click();
        } catch (NoSuchElementException e) {
            LOG.trace("majorLink: " + majorLink + "does not have any subscribers");
            return majorLink;
        }
        engine.waitExactly(3);

        while (handledSubscribersCount < subscribersHandleLimit) {
            try {
                temp = chrome.findElementByXPath(
                                String.format(
                                        engine.getXpaths().get("follower"), ++handledSubscribersCount));
            } catch (NoSuchElementException e) {
                System.out.println(handledSubscribersCount);
                return majorLink;
            }

            link.append(engine.getUrl().get("instagram")).append(temp.getAttribute("title")).append("/");

            likeTenSubscriberPhoto(link.toString());
            System.out.println(handledSubscribersCount);
            link.setLength(0);
        }
        return majorLink;
    }

    private void likeTenSubscriberPhoto(String link) {
        int count = 0;
        engine.humanImitation();
        chrome.switchTo().window(tabs.get(1)); //switches to new tab
        engine.waitExactly(1);
        chrome.get(link); //open subscriber link in a new tab

        if (findFirst()) {
            likeCurrent();
            count++;
            while (getNext() && count < photosToLike) {
                likeCurrent();
                count++;
            }
        }
        engine.waitExactly(1);
        chrome.switchTo().window(tabs.get(0));
        engine.waitExactly(1);
    }

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
     * Perform like on current photo
     * TODO 08/06/21 Не работает корректно, не видит поставлен лайк или нет
     */
    private void likeCurrent() {
        engine.waitExactly(1);
        WebElement likeButton = chrome.findElementByXPath(engine.getXpaths().get("like_button"));
        try {
            chrome.findElementByClassName("glyphsSpriteHeart__filled__24__red_5");
        }
        catch (NoSuchElementException ex) {
            likeButton.click();
            engine.humanImitation();
        }
    }

    private boolean getNext() {
        try {
            chrome.findElement(new By.ByClassName("coreSpriteRightPaginationArrow")).click();
            return true;
        }
        catch (NoSuchElementException e) { return false; }
    }
}
