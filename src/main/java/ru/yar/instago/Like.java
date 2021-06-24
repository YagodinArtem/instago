package ru.yar.instago;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
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

    /**
     * Сколько главных аккаунтов из списка будем обрабатывать
     */
    private int majorAccountsToHandle = 10;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Like(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        random = new Random();
        wasHandled = new ArrayList<>();
        links = new ArrayList<>();

        likeProcessor = new LikeProcessor(engine, chrome);

        initializeWork();

        startSearching();
        addAll();
        addLinks();

        start();
        closeAll();
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

            while (link != null) {
                wasHandled.add(link);
                link = reader.readLine();
            }

        } catch (IOException e) {
            LOG.error("Unable to read count from save.txt");
        }
    }

    /**
     * Start searching by input the search word (prop.properties var mainSearchWord).
     *
     * Начинает первичный поиск главных аккаунтов.
     */
    private void startSearching() {
        chrome.findElement(By.tagName("input"))
                .sendKeys(engine
                        .getPs()
                        .getMainSearchWord());
    }

    /**
     * Find and add all WebElements by xpath (major accounts) to the result List<WebElement>.
     *
     * Добавляет все найденные по ключевому слову главные аккаунты в список searchResult;
     */
    private void addAll() {
        engine.waitExactly(2);
        searchResult = chrome.findElements(By.tagName("a"));
    }

    /**
     * Adds all links from major accounts to List<String> links.
     *
     * Добавляет все прямые ссылки на главные аккаунты из списка searchResult в список links;
     */
    private void addLinks() {
        for (WebElement webElement : searchResult) links.add(webElement.getAttribute("href"));
    }


    /**
     * Start liking procedure majorAccountsToHandle is the limit to work with.
     * <p>
     * продумать сохранение текущих данных типа - major link - количество подписечников - на каком остановились.
     */
    private void start() {
        likeProcessor.createNewTab();
        int count = 0;
        String link;

        while (count < majorAccountsToHandle) {
            link = links.get(random.nextInt(links.size()));
            if (!wasHandled.contains(link)) {
                likeProcessor.startLiking(link);
                try {
                    wasHandled.add(link);
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

    /**
     * Close streams and browser driver.
     *
     * Закрывает потоки ввода вывода и драйвер браузера.
     */
    private void closeAll() {
        try {
            reader.close();
            writer.close();
            chrome.close();
        } catch (IOException e) {
            LOG.error("Unable to close streams");
        }
    }
}

