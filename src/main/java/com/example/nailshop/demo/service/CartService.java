
package com.example.nailshop.demo.service;

import com.example.nailshop.demo.model.CartItem;
import com.example.nailshop.demo.model.Product;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@SessionScope
//separate CartService for each HTPP session
public class CartService {

    private final Map<Long, CartItem> items = new LinkedHashMap<>();
    //LinkedHashMap preserves insertion order so products appear in the order they were added

    public boolean addProduct(Product product) {

    CartItem item = items.get(product.getId());

    int currentQuantity = item == null
            ? 0
            : item.getQuantity();

    int availableStock = product.getStock() == null
            ? 0
            : product.getStock();

    if (currentQuantity + 1 > availableStock) {
        return false;
    }

    if (item == null) {
    item = new CartItem(product, 1);
    items.put(product.getId(), item);
    } else {
        item.setProduct(product);
        item.setQuantity(currentQuantity + 1);
    }

    return true;
}

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public double getTotal() {

        double total = 0;

        for (CartItem item : items.values()) {
            total += item.getSubtotal();
        }

        return total;
    }

    public boolean updateQuantity(Product product, int quantity) {

    CartItem item = items.get(product.getId());

    if (item == null) {
        return false;
    }

    if (quantity <= 0) {
        items.remove(product.getId());
        return true;
    }

    int availableStock = product.getStock() == null
            ? 0
            : product.getStock();

    if (quantity > availableStock) {
        return false;
    }

    item.setProduct(product);
    item.setQuantity(quantity);

    return true;
    }

    public void removeProduct(Long productId) {
        items.remove(productId);
    }

    public void clearCart() {
    items.clear();
    }

}

