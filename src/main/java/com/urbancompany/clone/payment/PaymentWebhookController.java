package com.urbancompany.clone.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {
    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);
    private final PaymentService paymentService;
    public PaymentWebhookController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody String payload,
                                     @RequestHeader(value = "X-Razorpay-Signature", required = false) String sig) {
        if (!paymentService.verifyWebhook(payload, sig)) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid signature"));
        }
        log.info("[RAZORPAY WEBHOOK] {}", payload.length() > 500 ? payload.substring(0, 500) : payload);
        // TODO(prod): parse event=payment.captured / order.paid, mark ServiceRequest PAID, trigger payout split.
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of("live", paymentService.isLive(), "provider", "razorpay");
    }
}
