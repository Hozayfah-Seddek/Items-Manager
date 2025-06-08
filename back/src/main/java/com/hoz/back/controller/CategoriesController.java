package com.hoz.back.controller;

import com.hoz.back.model.Categories;
import com.hoz.back.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CategoriesController {

    @Autowired
    private CategoriesRepository categoriesRepository;


    @GetMapping("/categories")
    public List<Categories> getAllCategories() {
        return categoriesRepository.findAll();
    }
}
