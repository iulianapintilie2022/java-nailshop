package com.example.nailshop.demo.controller;

import com.example.nailshop.demo.model.Category;
import com.example.nailshop.demo.repository.CategoryRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nailshop.demo.repository.ProductRepository;

@Controller
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryController(CategoryRepository categoryRepository,
        ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
         this.productRepository = productRepository;
    }

    @GetMapping("/admin/categories")
    public String showCategories(Model model) {

        model.addAttribute("categories", categoryRepository.findAll());

        return "admin-categories";
    }

    @GetMapping("/admin/categories/new")
    public String showNewCategoryForm(Model model) {

        model.addAttribute("category", new Category());

        return "category-form";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(
        @ModelAttribute Category category,
        Model model) {

    Integer discount = category.getDiscountPercentage();

    if (discount == null) {
        category.setDiscountPercentage(0);
    } else if (discount < 0 || discount > 100) {
        model.addAttribute(
            "error",
            "Discount must be between 0 and 100%."
        );

        return "category-form";
    }

    categoryRepository.save(category);

    return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/edit/{id}")
    public String showEditCategoryForm(
        @PathVariable Long id,
        Model model) {

    Category category = categoryRepository
            .findById(id)
            .orElseThrow();

    model.addAttribute("category", category);

    return "category-form";
    }

    @PostMapping("/admin/categories/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (productRepository.existsByCategoryId(id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "This category cannot be deleted because it still contains products."
            );

            return "redirect:/admin/categories";
        }

        categoryRepository.deleteById(id);

        return "redirect:/admin/categories";
}
}