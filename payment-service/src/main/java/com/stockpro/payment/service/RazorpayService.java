package com.stockpro.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.stockpro.payment.dto.RazorpayOrderRequest;
import com.stockpro.payment.dto.RazorpayOrderResponse;
import com.stockpro.payment.dto.RazorpayVerifyRequest;
import com.stockpro.payment.entity.Payment;
import com.stockpro.payment.entity.PaymentMethod;
import com.stockpro.payment.entity.PaymentStatus;
import com.stockpro.payment.exception.ResourceNotFoundException;
import com.stockpro.payment.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private RazorpayClient client;

    @PostConstruct
    public void init() throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
    }

    @Transactional
    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) throws RazorpayException {
        // 1. Create internal pending payment record
        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .purchaseOrderId(request.getPurchaseOrderId())
                .supplierId(request.getSupplierId())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.UPI) // Default for Razorpay, can be any online method
                .status(PaymentStatus.PENDING)
                .notes(request.getNotes())
                .paymentDate(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 2. Create Razorpay order
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount().multiply(new BigDecimal(100)).intValue()); // Convert to paise
        orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
        orderRequest.put("receipt", savedPayment.getPaymentNumber());
        
        Order order = client.orders.create(orderRequest);

        // 3. Update payment with Razorpay Order ID
        savedPayment.setTransactionReference(order.get("id"));
        paymentRepository.save(savedPayment);

        return RazorpayOrderResponse.builder()
                .razorpayOrderId(order.get("id"))
                .amount(request.getAmount())
                .currency(order.get("currency"))
                .status(order.get("status"))
                .keyId(keyId)
                .paymentId(savedPayment.getPaymentId())
                .paymentNumber(savedPayment.getPaymentNumber())
                .build();
    }

    @Transactional
    public boolean verifyPayment(RazorpayVerifyRequest request) {
        try {
            // 1. Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                // 2. Update payment status to COMPLETED
                Payment payment = paymentRepository.findById(request.getInternalPaymentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", request.getInternalPaymentId()));
                
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionReference(request.getRazorpayPaymentId()); // Store the actual payment ID
                paymentRepository.save(payment);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generatePaymentNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAY-RZP-" + datePart + "-" + (counter.incrementAndGet() % 100000);
    }
}
