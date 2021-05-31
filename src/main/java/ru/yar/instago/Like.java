package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;

public class Like {

    private Engine engine;
    private ChromeDriver chrome;
    private List<WebElement> searchResult;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Like(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        startSearching();
        addAll();
        likeItOn();
    }

    /**
     * Start searching by inputting the search word (prop.properties var mainSearchWord)
     */
    private void startSearching() {
        chrome.findElementByXPath(engine.getXpaths().get("search_after_login"))
                .sendKeys(engine
                        .getPs()
                        .getMainSearchWord());
    }


    /**
     * Add all search results to the result List<WebElement>
     */
    private void addAll() {
        searchResult = new ArrayList<>();
        int count = 0;
        while (count < 5) {
            try {
                engine.waitExactly(1);
                searchResult.add(chrome
                        .findElementByXPath((String.format("//*[@id=\"react-root\"]/section/nav/div[2]/div/div/div[2]/div[3]/div/div[2]/div/div[%d]", ++count))));
            } catch (NullPointerException | IllegalArgumentException | NoSuchElementException e) {
                LOG.trace("Search result list is end");
                return;
            }
        }
    }

    private void likeItOn() {
        searchResult.get(0).click();
    }
}

