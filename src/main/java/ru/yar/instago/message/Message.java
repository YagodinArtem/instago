package ru.yar.instago.message;

import org.openqa.selenium.chrome.ChromeDriver;
import ru.yar.instago.Engine;

import java.util.HashSet;
import java.util.Set;

public class Message {

    //TODO это :D

    private Engine engine;
    private ChromeDriver chrome;
    private Set<String> links;


    public Message(Engine engine, ChromeDriver chrome) {
        this.engine = engine;
        this.chrome = chrome;
        links = new HashSet<>();
    }
}
