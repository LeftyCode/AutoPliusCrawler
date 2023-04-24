package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.Auto;
import com.leftycode.autoscarper.entity.AutoPicture;
import com.leftycode.autoscarper.repo.AutoPictureRepo;
import com.leftycode.autoscarper.repo.AutoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class FilterAutoPictures implements Runnable {

    private final AutoPictureRepo autoPictureRepo;

    @Override
    public void run() {
        int page = 104;
        PageRequest pageable = PageRequest.of(page, 100);
        var pictures = autoPictureRepo.findAll(pageable);
        long total = pictures.getTotalElements();
        int[] count = {0};
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(20);
        List<CountDownLatch> countDownLatchList = new ArrayList<>();
        while (!pictures.isEmpty()) {
            pictures.forEach(picture -> {
                count[0]++;
                log.info(count[0] + " / " + total + " pictureId: " + picture.getId());
                CountDownLatch countDownLatch = new CountDownLatch(1);
                countDownLatchList.add(countDownLatch);
                executor.submit(() -> {
                    try {
                        validatePicture(picture);
                    } catch (Exception e) {
                        log.error("picture id: {}", picture.getId(), e);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            });
            page += 100;
            pageable = PageRequest.of(page, 100);
            log.info("loading next batch...");
            pictures = autoPictureRepo.findAll(pageable);
        }
        countDownLatchList.forEach(countDownLatch -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("", e);
            }
        });
    }

    private void validatePicture(AutoPicture autoPicture) {
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8000/detect")
                .build();
        var response = client
                .post()
                .body(BodyInserters.fromValue(autoPicture.getPicture()))
                .retrieve()
                .bodyToMono(Float.class)
                .block();
        var pct = Optional.ofNullable(response).orElse(0f);
        autoPicture.setPctValid(pct);
        autoPicture.setValid(pct > 0.5 ? 1 : 0);
        autoPictureRepo.save(autoPicture);
    }
}
