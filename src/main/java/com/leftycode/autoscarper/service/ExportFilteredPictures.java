package com.leftycode.autoscarper.service;

import com.leftycode.autoscarper.entity.AutoPicture;
import com.leftycode.autoscarper.repo.AutoModelRepo;
import com.leftycode.autoscarper.repo.AutoPictureRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportFilteredPictures implements Runnable {

    private final AutoPictureRepo autoPictureRepo;

    private final String mainPath = Paths.get("").toAbsolutePath() + "/src/main/resources";

    @Override
    public void run() {
        File folder = new File(mainPath + "/filtered");
        if (folder.mkdirs()) {
            log.info("Created filtered folder successfully.");
        }
        var validPics = autoPictureRepo.findAllByValid(1, PageRequest.of(0, 1000));
        File valid = new File(folder.getAbsolutePath() + "/valid");
        valid.mkdirs();
        validPics.forEach(picture -> savePicture(picture, valid.getAbsolutePath()));

        var invalidPics = autoPictureRepo.findAllByValid(0, PageRequest.of(0, 1000));
        File invalid = new File(folder.getAbsolutePath() + "/invalid");
        invalid.mkdirs();
        invalidPics.forEach(picture -> savePicture(picture, invalid.getAbsolutePath()));
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
