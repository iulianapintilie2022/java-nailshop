
package com.example.nailshop.demo.service;

import com.example.nailshop.demo.model.*;
import com.example.nailshop.demo.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.stripe.model.checkout.Session;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository) {

                this.orderRepository = orderRepository;
                this.orderItemRepository = orderItemRepository;
                this.productRepository = productRepository;
    }

        @Transactional
        public CustomerOrder placeOrder(
            Customer customer,
            String contactEmail,
            String recipientName,
            String shippingStreet,
            String shippingPostalCode,
            String shippingCity,
            String shippingCountry,
            List<CartItem> cartItems) {

                if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty.");
                }

                CustomerOrder order = new CustomerOrder();

                order.setCustomer(customer);
                order.setContactEmail(contactEmail);
                order.setRecipientName(recipientName);
                order.setShippingStreet(shippingStreet);
                order.setShippingPostalCode(shippingPostalCode);
                order.setShippingCity(shippingCity);
                order.setShippingCountry(shippingCountry);
                order.setOrderDate(LocalDateTime.now());
                order.setStatus("PENDING_PAYMENT");

                BigDecimal total = BigDecimal.ZERO;
                List<OrderItem> orderItems = new ArrayList<>();

                for (CartItem cartItem : cartItems) {

                Product product = productRepository
                        .findById(cartItem.getProduct().getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product no longer exists."
                                )
                        );

                int quantity = cartItem.getQuantity();

                if (quantity <= 0) {
                        throw new IllegalArgumentException(
                                "Invalid product quantity."
                        );
                }

                if (product.getStock() == null
                        || product.getStock() < quantity) {

                        throw new IllegalArgumentException(
                                "Not enough stock for " + product.getName()
                        );
                }

                if (product.getPrice() == null
                        || !Double.isFinite(product.getPrice())
                        || product.getPrice() < 0) {

                        throw new IllegalArgumentException(
                                "Invalid price for " + product.getName()
                        );
                }

               BigDecimal unitPrice = product.getDiscountedPrice();

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(unitPrice);

                orderItems.add(orderItem);

                total = total.add(orderItem.getSubtotal());
        }

        order.setTotalPrice(total);

        CustomerOrder savedOrder = orderRepository.save(order);

        orderItemRepository.saveAll(orderItems);

        return savedOrder;
    }

        @Transactional
                public CustomerOrder saveOrder(CustomerOrder order) {
                return orderRepository.save(order);
        }


        @Transactional
                public void confirmStripePayment(Session session) {

                if (!"paid".equals(session.getPaymentStatus())) {
                        return;
                }

                CustomerOrder order = orderRepository
                        .findWithLockByStripeSessionId(session.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No order found for this Stripe session."
                        ));

                if ("PAID".equals(order.getStatus())
                        || "PAID_STOCK_ISSUE".equals(order.getStatus())) {
                        return;
                }

                if (!"PENDING_PAYMENT".equals(order.getStatus())) {
                        throw new IllegalStateException(
                                "Order is not awaiting payment."
                        );
                }

                if (!"eur".equalsIgnoreCase(session.getCurrency())
                        || session.getAmountTotal() == null
                        || order.getTotalPrice()
                                .movePointRight(2)
                                .longValueExact() != session.getAmountTotal()) {
                        throw new IllegalStateException(
                                "Stripe payment amount does not match order."
                        );
                }

                List<OrderItem> items =
                        orderItemRepository.findByOrderId(order.getId());

                boolean stockAvailable = true;

                for (OrderItem item : items) {
                        Product product = productRepository
                                .findById(item.getProduct().getId())
                                .orElseThrow();

                        if (product.getStock() == null
                                || product.getStock() < item.getQuantity()) {
                        stockAvailable = false;
                        }
                }

                if (!stockAvailable) {
                        order.setStatus("PAID_STOCK_ISSUE");
                        orderRepository.save(order);
                        return;
                }

                for (OrderItem item : items) {
                        Product product = productRepository
                                .findById(item.getProduct().getId())
                                .orElseThrow();

                        product.setStock(
                                product.getStock() - item.getQuantity()
                        );

                        productRepository.save(product);
                }

                order.setStatus("PAID");
                orderRepository.save(order);
        }

@Transactional
public void cancelUnpaidOrder(Long orderId) {

    CustomerOrder order = orderRepository
            .findById(orderId)
            .orElseThrow();

    if ("PENDING_PAYMENT".equals(order.getStatus())) {
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
}

}
