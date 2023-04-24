package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.AutoPicture;
import com.leftycode.autoscarper.repo.AutoPictureRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportFilteredPicturesAll implements Runnable {

    private final AutoPictureRepo autoPictureRepo;

    private final String mainPath = Paths.get("").toAbsolutePath() + "/src/main/resources";

    @Override
    public void run() {
        File folder = new File(mainPath + "/filtered");
        if (folder.mkdirs()) {
            log.info("Created filtered folder successfully.");
        }
        int page = 4;
        var pics = autoPictureRepo.findAll(PageRequest.of(page, 100));
        while (!pics.isEmpty()) {
            pics.forEach(picture -> savePicture(picture, folder.getAbsolutePath()));
            page += 100;
            pics = autoPictureRepo.findAll(PageRequest.of(page, 100));
        }
    }

    private void savePicture(
            final AutoPicture picture,
            final String absolutePath
    ) {
        try (FileOutputStream fileOutputStream =
                     new FileOutputStream(
                             absolutePath
                                     + "/"
                                     + picture.getId()
                                     + ".jpg"
                     )) {
            fileOutputStream.write(picture.getPicture());
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
