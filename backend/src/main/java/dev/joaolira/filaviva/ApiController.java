package dev.joaolira.filaviva;

import dev.joaolira.filaviva.ApiDtos.ActivityView;
import dev.joaolira.filaviva.ApiDtos.BookingView;
import dev.joaolira.filaviva.ApiDtos.CreateBooking;
import dev.joaolira.filaviva.ApiDtos.SlotView;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ActivityRepository activities;
    private final SlotRepository slots;
    private final BookingRepository bookings;
    private final BookingService bookingService;

    ApiController(ActivityRepository activities, SlotRepository slots, BookingRepository bookings, BookingService bookingService) {
        this.activities = activities;
        this.slots = slots;
        this.bookings = bookings;
        this.bookingService = bookingService;
    }

    @GetMapping("/activities")
    List<ActivityView> activities() {
        return activities.findAll().stream().map(ActivityView::from).toList();
    }

    @GetMapping("/slots")
    List<SlotView> slots() {
        Instant now = Instant.now();
        return slots.findByStartsAtBetweenOrderByStartsAtAsc(now, now.plus(14, ChronoUnit.DAYS)).stream()
            .map(slot -> new SlotView(slot.getId(), slot.getActivity().getId(), slot.getStartsAt(),
                !bookings.existsBySlotIdAndStatus(slot.getId(), Booking.Status.CONFIRMED),
                bookings.countBySlotIdAndStatus(slot.getId(), Booking.Status.WAITLISTED)))
            .toList();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    BookingView create(@Valid @RequestBody CreateBooking request) {
        return bookingService.create(request);
    }

    @GetMapping("/bookings/{code}")
    BookingView get(@PathVariable UUID code) {
        return bookingService.get(code);
    }

    @DeleteMapping("/bookings/{code}")
    BookingView cancel(@PathVariable UUID code) {
        return bookingService.cancel(code);
    }
}
