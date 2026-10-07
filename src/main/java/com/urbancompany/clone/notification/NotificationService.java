package com.urbancompany.clone.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Notification scaffold for Tier-3: WhatsApp first (highest open rate), SMS fallback.
 * With no tokens configured it logs + returns a wa.me link the UI can open.
 * Wire Meta WhatsApp Cloud API / MSG91 by filling env keys - call sites don't change.
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${whatsapp.token:}") private String whatsappToken;
    @Value("${whatsapp.phone-id:}") private String whatsappPhoneId;

    public String bookingConfirmWhatsApp(String phone, String service, String slot, String otp) {
        String msg = "Namaste! Your " + service + " booking is confirmed for " + slot + ". Share OTP " + otp + " only when the professional arrives. - HLSP Premnagar";
        send(phone, msg);
        return waLink(phone, msg);
    }

    public String providerNewJobWhatsApp(String phone, String service, String area, String slot) {
        String msg = "New job: " + service + " in " + area + " at " + slot + ". Open HLSP Partner app to Accept within 5 mins.";
        send(phone, msg);
        return waLink(phone, msg);
    }

    public void send(String phone, String message) {
        if (whatsappToken == null || whatsappToken.isBlank()) {
            log.info("[NOTIFY LOG-ONLY] to={} :: {}", phone, message);
            return;
        }
        // TODO(prod): POST https://graph.facebook.com/v19.0/{phoneId}/messages with template.
        log.info("[NOTIFY WHATSAPP QUEUED] to={}", phone);
    }

    public String waLink(String phone, String text) {
        String clean = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (clean.length() == 10) clean = "91" + clean;
        return "https://wa.me/" + clean + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
