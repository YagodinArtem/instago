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

    private List<WebElement> searchResult;
    private List<String> links;

    private int maxResultCapacity = 10;

    private BufferedReader reader;
    private BufferedWriter writer;

    private File save;

    private Random random;

    private static final Logger LOG = LogManager.getLogger(Engine.class.getName());

    public Like(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        random = new Random();
        searchResult = new ArrayList<>();
        links = new ArrayList<>();
        initializeWork();


        for (String link : links) {
            System.out.println(link);
        }

        startLiking();
    }

    /**
     * Start work with checking save file, if save file got links, load links to List<String>links,
     * else start searching by main search word
     */
    private void initializeWork() {
        save = new File("src/main/resources/save.txt");
        try {
            reader = new BufferedReader(new FileReader(save));
            writer = new BufferedWriter(new FileWriter(save, true));

            String link = reader.readLine();
            if (link != null && link.startsWith("http")) {
                while (link != null) {
                    links.add(link);
                    link = reader.readLine();
                }
            } else {
                startSearching();
                addAll();
                writeMajorAccs();
            }
        } catch (IOException e) {
            LOG.error("Unable to read count from save.txt");
        }
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
        int quantity = 0;
        while (quantity < maxResultCapacity) {
            try {
                engine.waitExactly(1);
                searchResult.add(chrome
                        .findElementByXPath((String.format("//*[@id=\"react-root\"]/section/nav/div[2]/div/div/div[2]/div[3]/div/div[2]/div/div[%d]/a", ++quantity))));
            } catch (NullPointerException | IllegalArgumentException | NoSuchElementException e) {
                LOG.trace("Search list is ended");
                return;
            }
        }
        LOG.trace("Added " + maxResultCapacity + " search results");
    }

    private void writeMajorAccs() {
        try {
            for (WebElement webElement : searchResult) {
                links.add(webElement.getAttribute("href"));
                writer.write(webElement.getAttribute("href") + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            LOG.error("Unable to write a file");
        }
    }

    /*TODO update this method now it works, open subscriber in a new tab
      реализовать следующее: пересмотреть алгоритм, начинать работу с поиска всегда, затем начинать ставить лайки, но
      только если в файле НЕ содержится ссылки на major account, таким образом, будет добавляться в файл отработанный материал
      (логика обратная существующей) если в файле нет такого линка то начать лайкать, если есть выбрать другой линка из
      пула линков searchResult;
     */

    private void startLiking() {
        chrome.get(links.get(random.nextInt(links.size())));
        engine.waitExactly(3);
        chrome.findElementByXPath(engine.getXpaths().get("followers")).click();
        engine.waitExactly(3);


        //get element
        WebElement temp = chrome.findElementByXPath("/html/body/div[5]/div/div/div[2]/ul/div/li[1]/div/div[2]/div[1]/div/div/span/a");

        System.out.println(temp.getAttribute("title"));
        String link = "https://www.instagram.com/"+temp.getAttribute("title")+"/";
        System.out.println(link);

        String a = "window.open('about:blank','_blank');";
        ((JavascriptExecutor) chrome).executeScript(a);

        ArrayList<String> tabs = new ArrayList<>(chrome.getWindowHandles());
        chrome.switchTo().window(tabs.get(1)); //switches to new tab
        engine.waitExactly(1);

        chrome.get(link);
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

