package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.AutoBrand;
import com.leftycode.autoscarper.repo.AutoBrandRepo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Lazy
public class AutoBrandScraper implements Runnable {

    private final String url = "https://autoplius.lt/";

    private final AutoBrandRepo autoBrandRepo;

    @Override
    public void run() {
        scrape(url);
    }

    @SneakyThrows
    public void scrape(final String url) {
        Document document = Jsoup.connect(url).get();
        Element brandsBlock = document.selectFirst(".dropdown-options.js-options");
        if (brandsBlock == null) {
            throw new RuntimeException("Empty brands block.");
        }
        Elements brands = brandsBlock.select(".dropdown-option.js-option");
        brands.forEach(element -> {
            String name = element.attr("data-title");
            int count = Integer.parseInt(element.attr("data-badge"));
            int apId = Integer.parseInt(element.attr("data-value"));
            AutoBrand autoBrand = new AutoBrand();
            autoBrand.setName(name);
            autoBrand.setCount(count);
            autoBrand.setApId(apId);
            autoBrandRepo.save(autoBrand);
        });
    }
}
