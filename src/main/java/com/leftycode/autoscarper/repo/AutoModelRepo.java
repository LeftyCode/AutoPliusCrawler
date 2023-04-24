package com.leftycode.autoscarper.repo;

import com.leftycode.autoscarper.entity.AutoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutoModelRepo extends JpaRepository<AutoModel, Long> {

    List<AutoModel> findAllByFinishedIsTrue();
}
