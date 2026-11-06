
package com.example.nailshop.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import com.example.nailshop.demo.repository.CustomerOrderRepository;
import com.example.nailshop.demo.model.Address;
import com.example.nailshop.demo.model.Customer;
import com.example.nailshop.demo.repository.AddressRepository;
import com.example.nailshop.demo.repository.CustomerRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.example.nailshop.demo.model.CustomerOrder;
import com.example.nailshop.demo.model.OrderItem;
import com.example.nailshop.demo.repository.OrderItemRepository;

@Controller
public class AccountController {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AccountController(
        CustomerRepository customerRepository,
        AddressRepository addressRepository,
        CustomerOrderRepository orderRepository,
        OrderItemRepository orderItemRepository) {

        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
         this.orderItemRepository = orderItemRepository;
    }

    
@GetMapping("/account")
public String showAccount(
        Authentication authentication,
        Model model) {

    Customer customer = customerRepository
            .findByEmail(authentication.getName())
            .orElseThrow();

    boolean isAdmin = authentication.getAuthorities()
            .stream()
            .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN")
            );

    model.addAttribute("customer", customer);
    model.addAttribute("isAdmin", isAdmin);

    model.addAttribute(
            "addresses",
            addressRepository.findByCustomerId(customer.getId())
    );

    model.addAttribute("newAddress", new Address());

    model.addAttribute(
            "latestOrder",
            orderRepository
                    .findFirstByCustomerIdOrderByOrderDateDescIdDesc(
                            customer.getId()
                    )
                    .orElse(null)
    );

    return "account";
}


    @PostMapping("/account/address")
    public String newAddress(
            @ModelAttribute Address formAddress,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Customer customer = customerRepository
                .findByEmail(principal.getName())
                .orElseThrow();

        String street = clean(formAddress.getStreet());
        String houseNumber = clean(formAddress.getHouseNumber());
        String postalCode = clean(formAddress.getPostalCode());
        String city = clean(formAddress.getCity());
        String country = clean(formAddress.getCountry());

        if (street.isEmpty()
                || houseNumber.isEmpty()
                || postalCode.isEmpty()
                || city.isEmpty()
                || country.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please complete all address fields."
            );

            return "redirect:/account";
        }

        Address address = new Address();

        address.setStreet(street);
        address.setHouseNumber(houseNumber);
        address.setPostalCode(postalCode);
        address.setCity(city);
        address.setCountry(country);

        address.setCustomer(customer);

        addressRepository.save(address);

        redirectAttributes.addFlashAttribute(
                "success",
                "Your address has been saved successfully."
        );

        return "redirect:/account";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }


@GetMapping("/account/address/{id}/edit")
public String editAddressForm(
        @PathVariable Long id,
        Principal principal,
        Model model) {

    Customer customer = customerRepository
            .findByEmail(principal.getName())
            .orElseThrow();

    Address address = addressRepository
            .findById(id)
            .orElseThrow(() ->
                    new org.springframework.web.server
                            .ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND
                    )
            );

    if (address.getCustomer() == null
            || !customer.getId().equals(
                    address.getCustomer().getId())) {

        throw new org.springframework.web.server
                .ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND
        );
    }

    model.addAttribute("address", address);

    return "edit-address";
}


@PostMapping("/account/address/{id}/edit")
public String updateAddress(
        @PathVariable Long id,
        @RequestParam String street,
        @RequestParam String houseNumber,
        @RequestParam String postalCode,
        @RequestParam String city,
        @RequestParam String country,
        Principal principal,
        RedirectAttributes redirectAttributes) {

    Customer customer = customerRepository
            .findByEmail(principal.getName())
            .orElseThrow();

    Address address = addressRepository
            .findById(id)
            .orElseThrow(() ->
                    new org.springframework.web.server
                            .ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND
                    )
            );

    if (address.getCustomer() == null
            || !customer.getId().equals(
                    address.getCustomer().getId())) {

        throw new org.springframework.web.server
                .ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND
        );
    }

    street = street.trim();
    houseNumber = houseNumber.trim();
    postalCode = postalCode.trim();
    city = city.trim();
    country = country.trim();

    if (street.isEmpty()
            || houseNumber.isEmpty()
            || postalCode.isEmpty()
            || city.isEmpty()
            || country.isEmpty()) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Please complete all address fields."
        );

        return "redirect:/account/address/" + id + "/edit";
    }

    address.setStreet(street);
    address.setHouseNumber(houseNumber);
    address.setPostalCode(postalCode);
    address.setCity(city);
    address.setCountry(country);

    addressRepository.save(address);

    redirectAttributes.addFlashAttribute(
            "success",
            "Address updated successfully."
    );

    return "redirect:/account";
}


@PostMapping("/account/address/{id}/delete")
public String deleteAddress(
        @PathVariable Long id,
        Principal principal,
        RedirectAttributes redirectAttributes) {

    Customer customer = customerRepository
            .findByEmail(principal.getName())
            .orElseThrow();

    Address address = addressRepository
            .findById(id)
            .orElseThrow(() ->
                    new org.springframework.web.server
                            .ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND
                    )
            );

    if (address.getCustomer() == null
            || !customer.getId().equals(
                    address.getCustomer().getId())) {

        throw new org.springframework.web.server
                .ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND
        );
    }

    addressRepository.delete(address);

    redirectAttributes.addFlashAttribute(
            "success",
            "Address deleted successfully."
    );

    return "redirect:/account";
}



@GetMapping("/account/orders")
public String showOrders(
        Authentication authentication,
        Model model) {

    Customer customer = customerRepository
            .findByEmail(authentication.getName())
            .orElseThrow();

    List<CustomerOrder> orders = orderRepository
            .findByCustomerIdOrderByOrderDateDescIdDesc(
                    customer.getId()
            );

    Map<Long, List<OrderItem>> orderItems = new HashMap<>();

    for (CustomerOrder order : orders) {
        orderItems.put(
                order.getId(),
                orderItemRepository.findByOrderId(order.getId())
        );
    }

    model.addAttribute("orders", orders);
    model.addAttribute("orderItems", orderItems);

    return "orders";
}


}
