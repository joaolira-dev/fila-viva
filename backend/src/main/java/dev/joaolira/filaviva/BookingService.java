package dev.joaolira.filaviva;

import dev.joaolira.filaviva.ApiDtos.BookingView;
import dev.joaolira.filaviva.ApiDtos.CreateBooking;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {
    private final SlotRepository slots;
    private final BookingRepository bookings;

    BookingService(SlotRepository slots, BookingRepository bookings) {
        this.slots = slots;
        this.bookings = bookings;
    }

    @Transactional
    public BookingView create(CreateBooking request) {
        Slot slot = slots.findForUpdate(request.slotId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horário não encontrado"));
        if (!slot.getStartsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Horário já passou");
        }
        Booking.Status status = bookings.existsBySlotIdAndStatus(slot.getId(), Booking.Status.CONFIRMED)
            ? Booking.Status.WAITLISTED : Booking.Status.CONFIRMED;
        Booking booking = bookings.save(new Booking(slot, request.guestName().trim(), status));
        return BookingView.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingView get(UUID code) {
        return BookingView.from(bookings.findById(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada")));
    }

    @Transactional
    public BookingView cancel(UUID code) {
        Long slotId = bookings.findSlotId(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada"));
        Slot slot = slots.findForUpdate(slotId).orElseThrow();
        Booking booking = bookings.findById(code).orElseThrow();
        if (booking.getStatus() == Booking.Status.CANCELLED) return BookingView.from(booking);
        boolean wasConfirmed = booking.getStatus() == Booking.Status.CONFIRMED;
        booking.changeStatus(Booking.Status.CANCELLED);
        bookings.saveAndFlush(booking);
        if (wasConfirmed && slot.getStartsAt().isAfter(Instant.now())) {
            bookings.findFirstBySlotIdAndStatusOrderByCreatedAtAscIdAsc(slot.getId(), Booking.Status.WAITLISTED)
                .ifPresent(next -> {
                    next.changeStatus(Booking.Status.CONFIRMED);
                    bookings.save(next);
                });
        }
        return BookingView.from(booking);
    }
}
