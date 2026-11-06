
package com.example.nailshop.demo.service;

import com.example.nailshop.demo.model.CustomerOrder;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentService {

    public StripePaymentService(
            @Value("${stripe.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    public Session createCheckoutSession(
            CustomerOrder order,
            String baseUrl) throws StripeException {

        long amountInCents = order.getTotalPrice()
                .movePointRight(2)
                .longValueExact();

        if (amountInCents <= 0) {
            throw new IllegalArgumentException(
                    "Order total must be greater than zero."
            );
        }

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setCustomerEmail(order.getContactEmail())
                        .setClientReferenceId(order.getId().toString())
                        .setSuccessUrl(
                                baseUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}"
                        )
                        .setCancelUrl(
                                baseUrl + "/checkout/cancel?order_id=" + order.getId()
                        )
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("eur")
                                                        .setUnitAmount(amountInCents)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("the nailshop - Order #" + order.getId())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        return Session.create(params);
    }
}

