package com.hoz.back.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateTransfareObject {
    private String name;
    private double amount;
    private String description;
    private String dateTime;
    private Long userId;
    private Integer categoryId;
    private String unitOfMeasurement;
    private double quantity;
    private double price;


}
