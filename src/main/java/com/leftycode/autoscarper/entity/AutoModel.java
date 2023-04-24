package com.leftycode.autoscarper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AutoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = AutoBrand.class)
    @JoinColumn
    private AutoBrand autoBrand;

    private String name;

    private int apId;

    private int count;

    private int pageNr;

    private boolean finished = false;
}
