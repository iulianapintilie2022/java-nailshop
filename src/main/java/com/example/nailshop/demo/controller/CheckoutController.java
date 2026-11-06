
package com.example.nailshop.demo.controller;

import com.example.nailshop.demo.model.Address;
import com.example.nailshop.demo.model.Customer;
import com.example.nailshop.demo.repository.CustomerOrderRepository;
import com.example.nailshop.demo.repository.CustomerRepository;
import com.example.nailshop.demo.repository.AddressRepository;
import com.example.nailshop.demo.service.CartService;
import com.example.nailshop.demo.model.CustomerOrder;
import com.example.nailshop.demo.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.nailshop.demo.service.StripePaymentService;
import org.springframework.beans.factory.annotation.Value;
import java.security.Principal;
import jakarta.servlet.http.HttpSession;
import java.util.regex.Pattern;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderService orderService;
    private final CustomerOrderRepository orderRepository;
    private final StripePaymentService stripePaymentService;
    private final String baseUrl;
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

    public CheckoutController(
            CartService cartService,
            CustomerRepository customerRepository,
            AddressRepository addressRepository,
            OrderService  orderService,
        StripePaymentService stripePaymentService,
        CustomerOrderRepository orderRepository,
        @Value("${app.base-url}") String baseUrl) {

        this.cartService = cartService;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.orderService = orderService;
        this.stripePaymentService = stripePaymentService;
        this.orderRepository = orderRepository;
        this.baseUrl = baseUrl;
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, Principal principal) {

        if (cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("items", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());

        if (principal != null) {

            Customer customer = customerRepository
                    .findByEmail(principal.getName())
                    .orElseThrow();

            model.addAttribute("customer", customer);

            model.addAttribute(
                    "addresses",
                    addressRepository.findByCustomerId(customer.getId())
            );

        } else {

            model.addAttribute("customer", null);
            model.addAttribute("addresses", java.util.List.of());
        }

        return "checkout";
    }



@PostMapping("/checkout/place-order")
public String placeOrder(
        @RequestParam String contactEmail,
        @RequestParam String recipientName,

        @RequestParam(required = false) Long savedAddressId,

        @RequestParam(required = false) String shippingStreet,
        @RequestParam(required = false) String shippingPostalCode,
        @RequestParam(required = false) String shippingCity,
        @RequestParam(required = false) String shippingCountry,

        Principal principal,
        RedirectAttributes redirectAttributes,
        HttpSession httpSession) {

    if (cartService.getItems().isEmpty()) {
        return "redirect:/cart";
    }

    Customer customer = null;

    if (principal != null) {
        customer = customerRepository
                .findByEmail(principal.getName())
                .orElseThrow();
    }

    contactEmail = clean(contactEmail);
    recipientName = clean(recipientName);

    if (!EMAIL_PATTERN.matcher(contactEmail).matches()
        || recipientName.isEmpty()) {

    redirectAttributes.addFlashAttribute(
            "error",
            "Please enter a valid email address and full name."
    );

    return "redirect:/checkout";
}

    if (savedAddressId != null) {

        if (customer == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please log in to use a saved address."
            );

            return "redirect:/checkout";
        }

        Address selectedAddress = addressRepository
                .findById(savedAddressId)
                .orElse(null);

        if (selectedAddress == null
                || selectedAddress.getCustomer() == null
                || !customer.getId().equals(
                        selectedAddress.getCustomer().getId())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "The selected address is not available."
            );

            return "redirect:/checkout";
        }

        shippingStreet = selectedAddress.getStreet()
                + " " + selectedAddress.getHouseNumber();

        shippingPostalCode = selectedAddress.getPostalCode();
        shippingCity = selectedAddress.getCity();
        shippingCountry = selectedAddress.getCountry();
    }

    // OPTION 2: Use a manually entered address.
    shippingStreet = clean(shippingStreet);
    shippingPostalCode = clean(shippingPostalCode);
    shippingCity = clean(shippingCity);
    shippingCountry = clean(shippingCountry);

    if (shippingStreet.isEmpty()
            || shippingPostalCode.isEmpty()
            || shippingCity.isEmpty()
            || shippingCountry.isEmpty()) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Please complete all delivery fields."
        );

        return "redirect:/checkout";
    }

    
try {
    CustomerOrder order = orderService.placeOrder(
            customer,
            contactEmail,
            recipientName,
            shippingStreet,
            shippingPostalCode,
            shippingCity,
            shippingCountry,
            new ArrayList<>(cartService.getItems())
    );

    // Remember the order for guest checkout.
    httpSession.setAttribute(
            "checkoutOrderId",
            order.getId()
    );

    // Clear the cart after the order is saved.
    cartService.clearCart();

    // Redirect to the order confirmation page.
    // Stripe payment happens separately.
    return "redirect:/orders/" + order.getId();

} catch (IllegalArgumentException exception) {

    redirectAttributes.addFlashAttribute(
            "error",
            exception.getMessage()
    );

    return "redirect:/checkout";
}

}

private String clean(String value) {
    return value == null ? "" : value.trim();
}


@GetMapping("/checkout/success")
public String paymentSuccess(
        @RequestParam("session_id") String sessionId,
        Model model) {

    CustomerOrder order = orderRepository
            .findByStripeSessionId(sessionId)
            .orElse(null);

    if (order == null) {
        model.addAttribute("paymentStatus", "UNKNOWN");
        return "payment-success";
    }

    if ("PAID".equals(order.getStatus())) {

        model.addAttribute("paymentStatus", "PAID");

    } else if ("PAID_STOCK_ISSUE".equals(order.getStatus())) {

        model.addAttribute(
                "paymentStatus",
                "PAID_STOCK_ISSUE"
        );

    } else {

        model.addAttribute("paymentStatus", "PENDING");
    }

    return "payment-success";
}



@GetMapping("/checkout/cancel")
public String paymentCancelled(
        @RequestParam("order_id") Long orderId) {
        // Leaving Stripe does not cancel the order.
    // The customer can return and pay later.
    return "redirect:/orders/" + orderId;
}

}

