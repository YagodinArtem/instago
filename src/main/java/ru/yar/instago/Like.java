package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Like {

    private Engine engine;
    private ChromeDriver chrome;
    private LikeProcessor likeProcessor;

    private List<WebElement> searchResult;
    private List<String> links;
    private List<String> wasHandled;


    private BufferedReader reader;
    private BufferedWriter writer;

    private File save;

    private Random random;

    private int majorAccountsToHandle = 10;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Like(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        random = new Random();
        searchResult = new ArrayList<>();
        wasHandled = new ArrayList<>();
        links = new ArrayList<>();

        likeProcessor = new LikeProcessor(engine, chrome);

        initializeWork();

        startSearching();
        addAll();
        addLinks();

        startLiking();
    }

    /**
     * Initialize work with save.txt file, when major account link was handled it adds to save.txt.
     * At start of the program read all handled before links to wasHandled List, then when we go to like some
     * followers from such link it will no be able.
     */
    private void initializeWork() {
        save = new File("src/main/resources/save.txt");
        try {
            reader = new BufferedReader(new FileReader(save));
            writer = new BufferedWriter(new FileWriter(save, true));

            String link = reader.readLine();
            if (link != null && link.startsWith("http")) {
                while (link != null) {
                    wasHandled.add(link);
                    link = reader.readLine();
                }
            }

        } catch (IOException e) {
            LOG.error("Unable to read count from save.txt");
        }
    }

    /**
     * Start searching by input the search word (prop.properties var mainSearchWord)
     */
    private void startSearching() {
        chrome.findElementByXPath(engine.getXpaths().get("search_after_login"))
                .sendKeys(engine
                        .getPs()
                        .getMainSearchWord());
    }

    /**
     * Find and add all WebElements by xpath (major accounts) to the result List<WebElement>
     */
    private void addAll() {
        int quantity = 0;
        while (true) {
            try {
                engine.waitExactly(1);
                searchResult.add(chrome
                        .findElementByXPath((String.format("//*[@id=\"react-root\"]/section/nav/div[2]/div/div/div[2]/div[3]/div/div[2]/div/div[%d]/a", ++quantity))));
            } catch (NullPointerException | IllegalArgumentException | NoSuchElementException e) {
                LOG.trace("Added " + quantity + " search results");
                return;
            }
        }
    }

    /**
     * Adds all links from major accounts to List<String> links
     */
    private void addLinks() {
        for (WebElement webElement : searchResult) links.add(webElement.getAttribute("href"));
    }


    /**
     * Start liking procedure majorAccountsToHandle is the limit to work with
     *
     * продумать сохранение текущих данных типа - major link - количество подписечников - на каком остановились
     */
    private void startLiking() {
        likeProcessor.createNewTab();
        int count = 0;

        while (count < majorAccountsToHandle) {

            String link = links.get(random.nextInt(links.size()));
            if (!wasHandled.contains(link)) {
                likeProcessor.startLiking(link);
                try {
                    writer.write(link);
                    writer.write("\n");
                    writer.flush();
                } catch (IOException e) {
                    LOG.error("Unable to write save.txt");
                }
            }
            count++;
        }
    }

    private void closeAll() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            LOG.error("Unable to close streams");
        }
    }
}

