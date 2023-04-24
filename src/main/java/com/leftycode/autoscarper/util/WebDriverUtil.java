package com.leftycode.autoscarper.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebDriverUtil {

    public void acceptCookies(WebDriver webDriver) {
        try {
            WebElement btn = webDriver.findElement(
                    By.cssSelector("#onetrust-accept-btn-handler")
            );
            clickAndWait(btn);
        } catch (NoSuchElementException e) {
            log.warn("Missing accept cookies");
        }
    }

    public void clickAndWait(WebElement btn) {
        btn.click();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadUrlAndWait(WebDriver webDriver, WebElement btn) {
        webDriver.navigate().to(btn.getAttribute("href"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadUrl(
            WebDriver webDriver,
            WebElement btn
    ) {
        webDriver.navigate().to(btn.getAttribute("href"));
    }

    public void loadUrl(
            final WebDriver webDriver,
            final String url
    ) {
        webDriver.get(url);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean blocked(Document document, String cssQuery) {
        return !document.select(cssQuery).isEmpty();
    }
}
