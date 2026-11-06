package com.example.nailshop.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.nailshop.demo.model.Product;
import com.example.nailshop.demo.repository.CategoryRepository;
import com.example.nailshop.demo.repository.ProductRepository;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductController(
        ProductRepository productRepository,
        CategoryRepository categoryRepository) {

    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
}

    @GetMapping("/admin/products")
    public String showProducts(Model model) {

        model.addAttribute(
            "products",
            productRepository.findAll()
        );

        return "admin-products";
    }
    @GetMapping("/admin/products/new")
    public String showNewProductForm(Model model) {

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());

        return "product-form";
    }

  @PostMapping("/admin/products")
public String saveProduct(
        @ModelAttribute("product") Product product,
        BindingResult bindingResult,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
        Model model
) throws IOException {
    if (bindingResult.hasErrors()
            || product.getName() == null || product.getName().isBlank()
            || product.getStock() == null || product.getStock() < 0
            || product.getPrice() == null || !Double.isFinite(product.getPrice())
            || product.getPrice() < 0) {
        model.addAttribute("error",
                "Please enter a product name, a whole-number stock of 0 or more, and a price of 0 or more.");
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-form";
    }

// If this is an existing product, keep its current image
    if (product.getId() != null) {

        Product existingProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        product.setImageUrl(existingProduct.getImageUrl());
    }

    if (imageFile != null && !imageFile.isEmpty()) {

        String originalFileName = imageFile.getOriginalFilename();

        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        String fileName = product.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                + extension;

        Path imagePath = Paths.get(
                        "src/main/resources/static/images/",
                        fileName
                );

        Files.copy(
                imageFile.getInputStream(),
                imagePath
        );

        product.setImageUrl("/images/" + fileName);
    }

    System.out.println("PRODUCT ID RECEIVED: " + product.getId());
    productRepository.save(product);

    return "redirect:/admin/products";
}

@GetMapping("/admin/products/edit/{id}")
public String showEditProductForm(
        @PathVariable Long id,
        Model model) {

    Product product = productRepository.findById(id)
            .orElseThrow();

    model.addAttribute("product", product);
    model.addAttribute("categories", categoryRepository.findAll());

    return "product-form";
}

@GetMapping("/admin/products/delete/{id}")
public String deleteProduct(@PathVariable Long id) {

    productRepository.deleteById(id);

    return "redirect:/admin/products";
}
}
