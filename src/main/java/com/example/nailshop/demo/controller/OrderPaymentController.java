
package com.example.nailshop.demo.controller;

import com.example.nailshop.demo.model.Customer;
import com.example.nailshop.demo.model.CustomerOrder;
import com.example.nailshop.demo.repository.CustomerOrderRepository;
import com.example.nailshop.demo.repository.CustomerRepository;
import com.example.nailshop.demo.service.OrderService;
import com.example.nailshop.demo.service.StripePaymentService;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class OrderPaymentController {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final StripePaymentService stripePaymentService;
    private final String baseUrl;

    public OrderPaymentController(
            CustomerOrderRepository orderRepository,
            CustomerRepository customerRepository,
            OrderService orderService,
            StripePaymentService stripePaymentService,
            @Value("${app.base-url}") String baseUrl) {

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderService = orderService;
        this.stripePaymentService = stripePaymentService;
        this.baseUrl = baseUrl;
    }

    // Verify ownership before displaying or paying an order.
    private CustomerOrder getAuthorizedOrder(
            Long orderId,
            Principal principal,
            HttpSession httpSession) {

        CustomerOrder order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND
                ));

        if (order.getCustomer() != null) {

            if (principal == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                );
            }

            Customer customer = customerRepository
                    .findByEmail(principal.getName())
                    .orElseThrow();

            if (!customer.getId().equals(
                    order.getCustomer().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                );
            }

        } else {

            Object guestOrderId =
                    httpSession.getAttribute("checkoutOrderId");

            if (!orderId.equals(guestOrderId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                );
            }
        }

        return order;
    }

    @GetMapping("/orders/{id}")
    public String showOrder(
            @PathVariable Long id,
            Principal principal,
            HttpSession httpSession,
            Model model) {

        CustomerOrder order = getAuthorizedOrder(
                id, principal, httpSession
        );

        model.addAttribute("order", order);

        return "order-confirmation";
    }

    @PostMapping("/orders/{id}/pay")
    public String payOrder(
            @PathVariable Long id,
            Principal principal,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes) {

        CustomerOrder order = getAuthorizedOrder(
                id, principal, httpSession
        );

        if ("PAID".equals(order.getStatus())
                || "PAID_STOCK_ISSUE".equals(order.getStatus())) {

            return "redirect:/orders/" + id;
        }

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "This order cannot be paid."
            );

            return "redirect:/orders/" + id;
        }

        try {

            // Reuse an existing open Stripe Checkout Session.
            if (order.getStripeSessionId() != null) {

                Session existingSession = Session.retrieve(
                        order.getStripeSessionId()
                );

                if ("paid".equals(
                        existingSession.getPaymentStatus())) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Payment received. Confirmation is processing."
                    );

                    return "redirect:/orders/" + id;
                }

                if ("open".equals(existingSession.getStatus())
                        && existingSession.getUrl() != null) {

                    return "redirect:" + existingSession.getUrl();
                }
            }

            // No reusable session: create one for this order.
            Session newSession =
                    stripePaymentService.createCheckoutSession(
                            order,
                            baseUrl
                    );

            order.setStripeSessionId(newSession.getId());
            orderService.saveOrder(order);

            return "redirect:" + newSession.getUrl();

        } catch (StripeException exception) {

            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Stripe payment could not be started. "
                    + "Your order is saved. Please try again."
            );

            return "redirect:/orders/" + id;
        }
    }
}
