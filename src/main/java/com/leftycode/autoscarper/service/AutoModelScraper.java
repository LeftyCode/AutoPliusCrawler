package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.AutoBrand;
import com.leftycode.autoscarper.entity.AutoModel;
import com.leftycode.autoscarper.repo.AutoBrandRepo;
import com.leftycode.autoscarper.repo.AutoModelRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class AutoModelScraper implements Runnable {

    private final AutoBrandRepo autoBrandRepo;

    private final AutoModelRepo autoModelRepo;

    @Override
    public void run() {
        var list = autoBrandRepo.findAll();
        int[] count = {0};
        list.forEach(autoBrand -> {
            count[0]++;
            log.info(count[0] + " / " + list.size());
            try {
                scrapeModels(autoBrand);
            } catch (RuntimeException e) {
                log.error("brand id: {}", autoBrand.getId(), e);
            }
        });
    }

    private void scrapeModels(final AutoBrand autoBrand) {
        WebClient client = WebClient.builder()
                .baseUrl("https://autoplius.lt/api/vehicle/models")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        var response = client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("make_id", autoBrand.getApId())
                        .queryParam("category_id", 2)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<APModelResponse>>() {})
                .block();
        if (CollectionUtils.isEmpty(response)) {
            throw new RuntimeException(
                    "Empty models response."
            );
        }
        response.forEach(apModelResponse -> convertAndSave(apModelResponse, autoBrand));
    }

    private void convertAndSave(
            final APModelResponse apModelResponse,
            final AutoBrand autoBrand
    ) {
        AutoModel autoModel = new AutoModel();
        autoModel.setAutoBrand(autoBrand);
        autoModel.setCount(apModelResponse.getBadge());
        autoModel.setApId(apModelResponse.getId());
        autoModel.setName(apModelResponse.getTitle());
        autoModelRepo.save(autoModel);
    }

    @Data
    static class APModelResponse {
        private int id;
        private String title;
        private int make_id;
        private int badge;
    }
}
