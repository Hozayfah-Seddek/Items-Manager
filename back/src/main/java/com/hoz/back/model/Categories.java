package com.hoz.back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Categories {
    @Id
    @Column(columnDefinition = "SMALLINT")
    private Integer id;
    private String name;

}
