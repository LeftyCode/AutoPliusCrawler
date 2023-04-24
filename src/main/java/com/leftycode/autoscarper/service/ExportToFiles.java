package com.leftycode.autoscarper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leftycode.autoscarper.repo.AutoModelRepo;
import com.leftycode.autoscarper.repo.AutoPictureRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportToFiles implements Runnable {

    private final AutoModelRepo autoModelRepo;

    private final AutoPictureRepo autoPictureRepo;

    private final String mainPath = Paths.get("").toAbsolutePath() + "/src/main/resources";

    @Override
    public void run() {
        var models = autoModelRepo.findAllByFinishedIsTrue();
        File folder = new File(mainPath + "/dataset");
        if (folder.mkdirs()) {
            log.info("Created dataset folder successfully.");
        }
        int[] count = {0};
        List<DatasetStatistic> datasetStatistics = new ArrayList<>();

        models.forEach(model -> {
            count[0]++;
            log.info(count[0] + " / " + models.size());
            String brandPath = folder.getAbsolutePath() + "/" + model.getAutoBrand().getName();
            String modelPath = brandPath + "_" + model.getName();
            var pictures = autoPictureRepo.findAllByAutoModelId(
                    model,
                    PageRequest.of(0, 1000)
            );
            if (pictures.isEmpty()) {
                return;
            }
            File modelFolder = new File(modelPath);
            modelFolder.mkdirs();
            DatasetStatistic datasetStatistic = new DatasetStatistic();
            datasetStatistic.setBrand(model.getAutoBrand().getName());
            datasetStatistic.setModel(model.getName());
            datasetStatistic.setCount(pictures.size());
            datasetStatistics.add(datasetStatistic);
            pictures.stream()
                    .filter(picture -> picture.getPicture() != null)
                    .forEach(picture -> {
                try {
                    try (FileOutputStream fileOutputStream =
                                 new FileOutputStream(
                                         modelPath
                                                 + "/"
                                                 + picture.getId()
                                                 + ".jpg"
                                 )) {
                        fileOutputStream.write(picture.getPicture());
                    }
                } catch (IOException e) {
                    log.error("", e);
                }
            });
        });
        System.out.println("Total: " + datasetStatistics.size());
        System.out.println("Total images: " + datasetStatistics.stream().mapToInt(DatasetStatistic::getCount).sum());
        datasetStatistics.sort(Comparator.comparing(DatasetStatistic::getBrand)
                .thenComparing(DatasetStatistic::getModel));
        try {
            System.out.println(new ObjectMapper().writeValueAsString(datasetStatistics));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Data
    static class DatasetStatistic {

        private String brand;

        private String model;

        private int count;
    }
}
