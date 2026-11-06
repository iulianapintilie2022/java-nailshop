
package com.example.nailshop.demo.controller;

import com.example.nailshop.demo.model.Product;
import com.example.nailshop.demo.repository.ProductRepository;
import com.example.nailshop.demo.service.CartService;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;

    public CartController(
            CartService cartService,
            ProductRepository productRepository) {

        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    @PostMapping("/cart/add/{id}")
    public ResponseEntity<String> addToCart(@PathVariable Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        boolean added = cartService.addProduct(product);

        if (!added) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Not enough stock available.");
        }

        return ResponseEntity.ok("Added to cart.");
    }

    @GetMapping("/cart")
    public String showCart(Model model) {

        model.addAttribute("items", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());

        return "cart";
    }
    
    @PostMapping("/cart/update/{id}")
    public String updateQuantity(
            @PathVariable Long id,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        boolean updated = cartService.updateQuantity(product, quantity);

        if (!updated) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Only " + product.getStock() + " " + product.getName() + " available in stock."
            );
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeCartItem(@PathVariable Long id) {

        cartService.removeProduct(id);

        return "redirect:/cart";
    }

}
