package com.example.nailshop.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.regex.Pattern;
import com.example.nailshop.demo.model.Customer;
import com.example.nailshop.demo.repository.CustomerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;


@Controller
public class RegistrationController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile(
                "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$"
        );


    
public RegistrationController(
        CustomerRepository customerRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager) {

    this.customerRepository = customerRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
}


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("customer", new Customer());
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(
        @ModelAttribute Customer customer,
        Model model,
        HttpServletRequest request,
        HttpServletResponse response) {
        
        String email = customer.getEmail() == null
            ? ""
            : customer.getEmail().trim();

    if (!EMAIL_PATTERN.matcher(email).matches()) {

        model.addAttribute(
                "error",
                "Please enter a complete email address, e.g. name@example.com."
        );

        return "register";
    }
    customer.setEmail(email);
    
    if (customerRepository.findByEmail(email).isPresent()) {

        model.addAttribute(
                "error",
                "An account with this email already exists."
        );

        return "register";
    }

    
String rawPassword = customer.getPassword();

customer.setPassword(
        passwordEncoder.encode(rawPassword)
);
customer.setRole("CUSTOMER");
customerRepository.save(customer);

// Authenticate the newly registered customer
Authentication authentication = authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(
                customer.getEmail(),
                rawPassword
        )
);

// Create authenticated security context
SecurityContext securityContext =
        SecurityContextHolder.createEmptyContext();

securityContext.setAuthentication(authentication);
SecurityContextHolder.setContext(securityContext);

// Save the authentication in user's HTTP session
request.changeSessionId();

securityContextRepository.saveContext(
        securityContext,
        request,
        response
);

// Redirect to account 
return "redirect:/account";

}
}
