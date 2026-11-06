package com.example.nailshop.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nailshop.demo.repository.CategoryRepository;
import com.example.nailshop.demo.repository.ProductRepository;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        // dependency injection
    }

    @GetMapping("/products")
public String showProducts(
        @RequestParam(required = false) Long categoryId,
        Model model) {
    if (categoryId != null) {
        //visiting /products with no categoryID => null => displays all products
        model.addAttribute(
            "products",
            productRepository.findByCategoryId(categoryId)
        );
    } else {
        model.addAttribute(
            "products",
            productRepository.findAll()
        );
    }

    model.addAttribute(
        "categories",
        categoryRepository.findAll()
    );

    return "products";
}
}
