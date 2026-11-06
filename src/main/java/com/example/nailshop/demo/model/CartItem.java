
package com.example.nailshop.demo.model;

import java.math.BigDecimal;

public class CartItem {

    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
    return product.getDiscountedPrice()
            .multiply(BigDecimal.valueOf(quantity))
            .doubleValue();
    }
}
