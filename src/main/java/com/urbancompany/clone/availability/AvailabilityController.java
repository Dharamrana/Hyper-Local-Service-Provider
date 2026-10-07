package com.urbancompany.clone.availability;

import com.urbancompany.clone.repository.ServiceProviderRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider/availability")
public class AvailabilityController {
    private final AvailabilityService service;
    private final ServiceProviderRepository providers;

    public AvailabilityController(AvailabilityService service, ServiceProviderRepository providers) {
        this.service = service; this.providers = providers;
    }

    private Long myId(Authentication auth) {
        return providers.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Provider account not found")).getId();
    }

    @GetMapping("/slots")
    public List<String> slots(Authentication auth,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.availableSlots(myId(auth), date);
    }

    @GetMapping("/day")
    public Map<String, Object> day(Authentication auth,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long pid = myId(auth);
        return Map.of("providerId", pid, "date", date.toString(),
                "availableSlots", service.availableSlots(pid, date),
                "overrides", service.dayView(pid, date),
                "onLeave", service.isOnLeave(pid, date));
    }

    @PostMapping("/slot")
    public ResponseEntity<?> setSlot(Authentication auth, @RequestBody Map<String, Object> body) {
        LocalDate date = LocalDate.parse(String.valueOf(body.get("date")));
        String slot = String.valueOf(body.get("slot"));
        boolean available = body.get("available") == null || Boolean.parseBoolean(String.valueOf(body.get("available")));
        Integer cap = body.get("capacity") != null ? Integer.parseInt(String.valueOf(body.get("capacity"))) : 1;
        String note = body.get("note") != null ? String.valueOf(body.get("note")) : null;
        return ResponseEntity.ok(service.setSlot(myId(auth), date, slot, available, cap, note));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leave(Authentication auth, @RequestBody Map<String, String> body) {
        var l = service.addLeave(myId(auth), LocalDate.parse(body.get("from")), LocalDate.parse(body.get("to")), body.get("reason"));
        return ResponseEntity.ok(l);
    }

    @GetMapping("/leaves")
    public List<?> leaves(Authentication auth) { return service.myLeaves(myId(auth)); }

    /** Public: customer booking wizard asks which slots a chosen pro can take. */
    @GetMapping("/public/{providerId}")
    public List<String> publicSlots(@PathVariable Long providerId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.availableSlots(providerId, date);
    }
}
