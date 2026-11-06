package com.example.nailshop.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity  
// java class representing a data base entity
public class Product {
    //one nailpolish could look like id:1, name: Milky Pink, description: Soft milky pink gel polish, price:12

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //id is primarykey, all values are mapped from mysql, ORM 

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String imageUrl;

@ManyToOne
private Category category;

public Product() {
}

public Product(String name, String description, Double price, Integer stock, String imageUrl) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.stock = stock;
    this.imageUrl = imageUrl;
}

public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public Double getPrice() {
    return price;
}

public void setPrice(Double price) {
    this.price = price;
}

public Integer getStock() {
    return stock;
}

public void setStock(Integer stock) {
    this.stock = stock;
}

public String getImageUrl() {
    return imageUrl;
}

public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
}

public Category getCategory() {
    return category;
}

public void setCategory(Category category) {
    this.category = category;
}

public BigDecimal getDiscountedPrice() {

    BigDecimal originalPrice = BigDecimal
            .valueOf(this.price)
            .setScale(2, RoundingMode.HALF_UP);

    if (category == null
            || category.getDiscountPercentage() == null
            || category.getDiscountPercentage() <= 0) {
        return originalPrice;
    }

    BigDecimal discountFactor = BigDecimal.ONE.subtract(
            BigDecimal.valueOf(category.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100))
    );

    return originalPrice
            .multiply(discountFactor)
            .setScale(2, RoundingMode.HALF_UP);
}
}