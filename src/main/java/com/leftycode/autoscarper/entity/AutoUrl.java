package com.leftycode.autoscarper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AutoUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = AutoModel.class)
    @JoinColumn
    private AutoModel autoModel;

    private String url;
}
