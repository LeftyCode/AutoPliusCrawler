package com.leftycode.autoscarper.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@Entity
@Data
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = AutoUrl.class)
    @JoinColumn
    private AutoUrl autoUrl;

    private Integer year;

    private Double price;

    private boolean invalid;
}
