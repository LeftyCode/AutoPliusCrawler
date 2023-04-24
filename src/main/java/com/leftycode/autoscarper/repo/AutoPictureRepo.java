package com.leftycode.autoscarper.repo;

import com.leftycode.autoscarper.entity.Auto;
import com.leftycode.autoscarper.entity.AutoModel;
import com.leftycode.autoscarper.entity.AutoPicture;
import com.leftycode.autoscarper.entity.AutoUrl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutoPictureRepo extends JpaRepository<AutoPicture, Long> {

    @Query(value = "select p from AutoPicture p inner join p.auto a inner join a.autoUrl u where u.autoModel = ?1")
    List<AutoPicture> findAllByAutoModelId(AutoModel autoModel, Pageable pageable);

    List<AutoPicture> findAllByValid(Integer valid, Pageable pageable);

    List<AutoPicture> findAllByAuto(Auto auto);
}
