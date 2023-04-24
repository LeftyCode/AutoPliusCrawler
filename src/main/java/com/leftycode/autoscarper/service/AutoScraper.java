package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.Auto;
import com.leftycode.autoscarper.entity.AutoPicture;
import com.leftycode.autoscarper.entity.AutoUrl;
import com.leftycode.autoscarper.repo.AutoPictureRepo;
import com.leftycode.autoscarper.repo.AutoRepo;
import com.leftycode.autoscarper.repo.AutoUrlRepo;
import com.leftycode.autoscarper.util.ImageDownloader;
import com.leftycode.autoscarper.util.WebDriverUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class AutoScraper implements Runnable {

    private final AutoUrlRepo autoUrlRepo;

    private final WebDriver webDriver;

    private final WebDriverUtil webDriverUtil;

    private final ImageDownloader imageDownloader;

    private final AutoRepo autoRepo;

    private final AutoPictureRepo autoPictureRepo;

    @Override
    public void run() {
        PageRequest pageable = PageRequest.of(0, 100);
        var urls = autoUrlRepo.findAll(pageable);
        long total = urls.getTotalElements();
        int[] count = {0};
        while (!urls.isEmpty()) {
            urls.forEach(url -> {
                count[0]++;
                log.info(count[0] + " / " + total + " url: " + url.getUrl());
                Auto auto = null;
                var list = autoRepo.findAutoByAutoUrl(url);
                if (!list.isEmpty()) {
                    return;
                }
                scrape(url, auto);
            });
            pageable = pageable.next();
            log.info("loading next batch...");
            urls = autoUrlRepo.findAll(pageable);
        }
    }

    private void scrape(AutoUrl autoUrl, Auto autoArg) {
        webDriver.navigate().to(autoUrl.getUrl());
        webDriverUtil.acceptCookies(webDriver);
        Document document = Jsoup.parse(webDriver.getPageSource());
        if (webDriverUtil.blocked(document, "img[src=https://autoplius.lt/utility/captcha/text]")) {
            throw new RuntimeException("Blocked by captcha.");
        }
        Auto auto = Optional.ofNullable(autoArg).orElse(new Auto());
        auto.setAutoUrl(autoUrl);
        auto.setPrice(parsePrice(document));
        auto.setYear(parseYear(document));
        autoRepo.save(auto);
        var pictures = parsePictures(document, auto);
        if (CollectionUtils.isEmpty(pictures)) {
            auto.setInvalid(true);
        } else {
            autoPictureRepo.saveAll(pictures);
        }
    }

    private Integer parseYear(Document document) {
        Elements elements = document.select(".parameter-row");
        Elements elements1 = elements.select("div:contains(Pagaminimo data)");
        Elements elements2 = elements1.select(".parameter-value");
        if (elements2.isEmpty()) {
            return null;
        }
        String year = elements2.get(0).text().trim();
        year = year.split("-")[0];
        return Integer.parseInt(year);
    }

    private Double parsePrice(Document document) {
        Element element = document.selectFirst("[data-price]");
        if (element != null) {
            return Double.parseDouble(element.attr("data-price"));
        }
        return null;
    }

    private List<AutoPicture> parsePictures(
            final Document document,
            final Auto auto
    ) {
        Elements elements = document.select("[id^=image_]");
        int count = 0;
        while (elements.isEmpty() && count < 10) {
            count++;
            elements = document.select("[id^=image_]");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
        if (elements.isEmpty()) {
            return new ArrayList<>();
        }
        List<AutoPicture> list = new ArrayList<>();
        elements.forEach(element -> {
            if (list.size() > 10) {
                return;
            }
            String url = Optional
                    .ofNullable(element.firstElementChild())
                    .map(e -> e.attr("data-src"))
                    .orElse(null);
            AutoPicture autoPicture = new AutoPicture();
            autoPicture.setUrl(url);
            autoPicture.setAuto(auto);
            if (StringUtils.hasLength(url)) {
                try {
                    autoPicture.setPicture(imageDownloader.download(url));
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            list.add(autoPicture);
        });
        return list;
    }
}
