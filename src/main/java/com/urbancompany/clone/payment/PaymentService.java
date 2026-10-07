package com.urbancompany.clone.payment;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Razorpay integration scaffold.
 * - When RAZORPAY_KEY_ID is blank -> MOCK mode (same UX, no real charge) so the
 *   app runs in college/demo without keys.
 * - When keys are present -> creates a real Razorpay Order; webhook confirms it.
 */
@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${razorpay.key-id:}")
    private String keyId;
    @Value("${razorpay.key-secret:}")
    private String keySecret;
    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    public boolean isLive() { return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank(); }

    /** Create order for a booking. amountPaise = rupees * 100. */
    public PaymentOrder createOrder(long bookingId, double amountRupees, String receipt) {
        int paise = (int) Math.round(amountRupees * 100);
        if (!isLive()) {
            String mockId = "order_MOCK_" + UUID.randomUUID().toString().substring(0, 12);
            log.info("[PAYMENT MOCK] order {} booking={} amount={} paise", mockId, bookingId, paise);
            return new PaymentOrder(mockId, paise, "INR", "created", keyId, true);
        }
        try {
            com.razorpay.RazorpayClient client = new com.razorpay.RazorpayClient(keyId, keySecret);
            JSONObject req = new JSONObject();
            req.put("amount", paise);
            req.put("currency", "INR");
            req.put("receipt", receipt != null ? receipt : ("hlsp_" + bookingId));
            req.put("payment_capture", 1);
            com.razorpay.Order order = client.orders.create(req);
            return new PaymentOrder(order.get("id"), order.get("amount"), order.get("currency"), order.get("status"), keyId, false);
        } catch (Exception e) {
            throw new IllegalStateException("Razorpay order failed: " + e.getMessage(), e);
        }
    }

    /** Verify webhook signature (Razorpay sends X-Razorpay-Signature = HMAC-SHA256(body, webhookSecret)). */
    public boolean verifyWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) return true; // dev: accept, log only
        if (signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return expected.equals(signature);
        } catch (Exception e) { return false; }
    }

    public record PaymentOrder(String id, int amountPaise, String currency, String status, String keyId, boolean mock) {}
}
