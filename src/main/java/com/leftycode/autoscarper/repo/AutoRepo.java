package com.leftycode.autoscarper.repo;

import com.leftycode.autoscarper.entity.Auto;
import com.leftycode.autoscarper.entity.AutoUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutoRepo extends JpaRepository<Auto, Long> {

    List<Auto> findAutoByAutoUrl(AutoUrl autoUrl);
}
