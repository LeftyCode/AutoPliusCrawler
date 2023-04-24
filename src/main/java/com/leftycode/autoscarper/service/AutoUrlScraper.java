package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.AutoModel;
import com.leftycode.autoscarper.entity.AutoUrl;
import com.leftycode.autoscarper.repo.AutoModelRepo;
import com.leftycode.autoscarper.repo.AutoUrlRepo;
import com.leftycode.autoscarper.util.WebDriverUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class AutoUrlScraper implements Runnable {

//    @Qualifier("webDriverHeadless")
    @Autowired
    private ObjectFactory<WebDriver> webDriverFactory;

    private final WebDriverUtil webDriverUtil;

    private static final String BASE_URL = "https://autoplius.lt/skelbimai/naudoti-automobiliai?" +
            "make_date_from=" +
            "&make_date_to=" +
            "&sell_price_from=" +
            "&sell_price_to=" +
            "&engine_capacity_from=" +
            "&engine_capacity_to=" +
            "&power_from=" +
            "&power_to=" +
            "&kilometrage_from=" +
            "&kilometrage_to=" +
            "&qt=" +
            "&qt_autocomplete=" +
            "&has_damaged_id=10924" +
            "&category_id=2" +
            "&make_id=%s" +
            "&model_id=%s" +
            "&page_nr=%s";

    private final AutoModelRepo autoModelRepo;

    private final AutoUrlRepo autoUrlRepo;

    @SneakyThrows
    @Override
    public void run() {
        var autoModels = autoModelRepo
                .findAll()
                .stream()
                .filter(autoModel -> autoModel.getCount() > 100)
                .filter(autoModel -> !autoModel.isFinished())
                .toList();
        int[] count = {0};
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(1);
        List<CountDownLatch> countDownLatchList = new ArrayList<>();
        autoModels.forEach(autoModel -> {
            count[0]++;
            log.info(count[0] + " / " + autoModels.size());
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatchList.add(countDownLatch);
            executor.submit(() -> {
                try {
                    scrape(autoModel);
                    autoModel.setFinished(true);
                    autoModelRepo.save(autoModel);
                } catch (Exception e) {
                    log.error("", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatchList.forEach(countDownLatch -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("", e);
            }
        });
    }

    private void scrape(
            final AutoModel autoModel
    ) {
        WebDriver webDriver = webDriverFactory.getObject();
        ((ChromeDriver) webDriver).executeCdpCommand(
                "Network.setUserAgentOverride",
                Map.of(
                        "userAgent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.53 Safari/537.36"
                ));
        ((ChromeDriver) webDriver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        try {
            int pageNr = autoModel.getPageNr();
            webDriverUtil.loadUrl(
                    webDriver,
                    BASE_URL.formatted(
                            autoModel.getAutoBrand().getApId(),
                            autoModel.getApId(),
                            pageNr
                    ));
            webDriverUtil.acceptCookies(webDriver);
            do {
                saveAutoUrls(webDriver, autoModel);
                pageNr++;
                autoModel.setPageNr(pageNr);
                autoModelRepo.save(autoModel);
                log.info("Saved: " + autoModel.getName() + " page_nr: " + pageNr);
            } while (loadNextPage(webDriver));
        } finally {
            webDriver.close();
        }
    }

    private void saveAutoUrls(final WebDriver webDriver, final AutoModel autoModel) {
        Document document = Jsoup.parse(webDriver.getPageSource());
        Elements elements = document.select(".announcement-item");
        int count = 0;
        while (count < 10 && elements.isEmpty()) {
            count ++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("", e);
            }
            elements = document.select(".announcement-item");
        }
        if (elements.isEmpty()) {
            log.warn("Missing auto elements: " + webDriver.getCurrentUrl());
            return;
        }
        elements.forEach(element -> {
            String url = element.attr("href");
            if (!StringUtils.hasLength(url)) {
                return;
            }
            AutoUrl autoUrl = new AutoUrl();
            autoUrl.setUrl(url);
            autoUrl.setAutoModel(autoModel);
            autoUrlRepo.save(autoUrl);
        });
    }

    private boolean loadNextPage(final WebDriver webDriver) {
        try {
            WebElement webElement = webDriver.findElement(By.className("next"));
            webDriverUtil.loadUrl(webDriver, webElement);
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }
}
