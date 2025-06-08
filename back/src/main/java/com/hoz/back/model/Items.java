package com.hoz.back.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private double amount;
    private String description;
    private LocalDateTime dateTime;
    private double quantity;
    private String unitOfMeasurement;
    private double price;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

}
