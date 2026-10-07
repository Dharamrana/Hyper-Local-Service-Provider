package com.urbancompany.clone.availability;

import com.urbancompany.clone.model.ProviderAvailability;
import com.urbancompany.clone.model.ProviderLeave;
import com.urbancompany.clone.model.ServiceProvider;
import com.urbancompany.clone.repository.ProviderAvailabilityRepository;
import com.urbancompany.clone.repository.ProviderLeaveRepository;
import com.urbancompany.clone.repository.ServiceProviderRepository;
import com.urbancompany.clone.repository.ServiceRequestRepository;
import com.urbancompany.clone.service.ServiceRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AvailabilityService {
    private final ProviderAvailabilityRepository slots;
    private final ProviderLeaveRepository leaves;
    private final ServiceProviderRepository providers;
    private final ServiceRequestRepository requests;

    public AvailabilityService(ProviderAvailabilityRepository slots, ProviderLeaveRepository leaves,
                               ServiceProviderRepository providers, ServiceRequestRepository requests) {
        this.slots = slots; this.leaves = leaves; this.providers = providers; this.requests = requests;
    }

    public boolean isOnLeave(Long providerId, LocalDate date) {
        return !leaves.findByProviderIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(providerId, date, date).isEmpty();
    }

    /** Slots the customer can actually book for this provider on this date. */
    public List<String> availableSlots(Long providerId, LocalDate date) {
        ServiceProvider p = providers.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        if (!Boolean.TRUE.equals(p.getIsAvailable()) || isOnLeave(providerId, date)) return List.of();
        var overrides = slots.findByProviderIdAndDate(providerId, date);
        return ServiceRequestService.DAILY_SLOTS.stream().filter(slot -> {
            var ov = overrides.stream().filter(o -> slot.equals(o.getSlot())).findFirst();
            if (ov.isPresent() && !Boolean.TRUE.equals(ov.get().getAvailable())) return false;
            int cap = ov.map(o -> o.getCapacity() != null ? o.getCapacity() : 1).orElse(1);
            long booked = requests.findByProviderId(providerId).stream()
                    .filter(r -> date.equals(r.getScheduledDate()) && slot.equals(r.getScheduledSlot())
                            && r.getStatus() != null && r.getStatus().name().matches("PENDING|ASSIGNED|IN_PROGRESS"))
                    .count();
            return booked < cap;
        }).toList();
    }

    public boolean isAvailable(Long providerId, LocalDate date, String slot) {
        return availableSlots(providerId, date).contains(slot);
    }

    public ProviderAvailability setSlot(Long providerId, LocalDate date, String slot,
                                        boolean available, Integer capacity, String note) {
        if (!ServiceRequestService.DAILY_SLOTS.contains(slot)) {
            throw new IllegalArgumentException("Invalid slot. Choose one of " + ServiceRequestService.DAILY_SLOTS);
        }
        ProviderAvailability row = slots.findByProviderIdAndDateAndSlot(providerId, date, slot)
                .orElse(new ProviderAvailability(null, providerId, date, slot, available, capacity, note));
        row.setAvailable(available);
        if (capacity != null) row.setCapacity(capacity);
        if (note != null) row.setNote(note);
        return slots.save(row);
    }

    public ProviderLeave addLeave(Long providerId, LocalDate from, LocalDate to, String reason) {
        if (to.isBefore(from)) throw new IllegalArgumentException("Leave end cannot be before start");
        return leaves.save(new ProviderLeave(null, providerId, from, to, reason));
    }

    public List<ProviderAvailability> dayView(Long providerId, LocalDate date) {
        return slots.findByProviderIdAndDate(providerId, date);
    }
    public List<ProviderLeave> myLeaves(Long providerId) { return leaves.findByProviderId(providerId); }
}
