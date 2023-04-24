package com.leftycode.autoscarper.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Data
public class AutoPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] picture;

    @ManyToOne(targetEntity = Auto.class)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn
    private Auto auto;

    private Float pctValid;

    private Integer valid;
}
