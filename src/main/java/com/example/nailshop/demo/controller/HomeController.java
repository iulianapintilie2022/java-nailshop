package com.example.nailshop.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import com.example.nailshop.demo.repository.CategoryRepository;
import com.example.nailshop.demo.repository.ProductRepository;

@Controller
public class HomeController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    public HomeController(
            CategoryRepository categoryRepository,
            ProductRepository productRepository) {

        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

@GetMapping("/")
public String home(Model model) {

        model.addAttribute(
            "categories",
            categoryRepository.findAll()
        );

        model.addAttribute(
            "featuredProducts",
            productRepository.findAll()
                .stream()
                .limit(4)
                .toList()
        );
          return "index";
    }


@GetMapping("/account-link")
public String accountLink(Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    }

    return "redirect:/account";
}

}
