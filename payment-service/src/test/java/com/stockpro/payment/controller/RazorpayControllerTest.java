package com.stockpro.payment.controller;

import com.razorpay.RazorpayException;
import com.stockpro.payment.dto.RazorpayOrderRequest;
import com.stockpro.payment.dto.RazorpayOrderResponse;
import com.stockpro.payment.dto.RazorpayVerifyRequest;
import com.stockpro.payment.service.RazorpayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayControllerTest {

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private RazorpayController razorpayController;

    @Test
    void createOrderAndVerifyPaymentReturnExpectedResponses() throws RazorpayException {
        RazorpayOrderRequest orderRequest = RazorpayOrderRequest.builder().amount(new BigDecimal("100.00")).build();
        RazorpayOrderResponse orderResponse = RazorpayOrderResponse.builder().razorpayOrderId("order_1").build();
        RazorpayVerifyRequest verifyRequest = RazorpayVerifyRequest.builder()
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .razorpaySignature("signature")
                .build();

        when(razorpayService.createOrder(orderRequest)).thenReturn(orderResponse);
        when(razorpayService.verifyPayment(verifyRequest)).thenReturn(true, false);

        assertThat(razorpayController.createOrder(orderRequest).getBody()).isSameAs(orderResponse);
        assertThat(razorpayController.verifyPayment(verifyRequest).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(razorpayController.verifyPayment(verifyRequest).getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void createOrderReturnsServerErrorWhenRazorpayFails() throws RazorpayException {
        RazorpayOrderRequest orderRequest = RazorpayOrderRequest.builder().amount(new BigDecimal("100.00")).build();
        when(razorpayService.createOrder(orderRequest)).thenThrow(new RazorpayException("down"));

        assertThat(razorpayController.createOrder(orderRequest).getStatusCode().is5xxServerError()).isTrue();
    }
}
