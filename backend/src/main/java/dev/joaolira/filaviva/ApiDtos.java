package dev.joaolira.filaviva;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {}
    public record ActivityView(Long id, String name, String description, int durationMinutes) {
        static ActivityView from(Activity activity) {
            return new ActivityView(activity.getId(), activity.getName(), activity.getDescription(), activity.getDurationMinutes());
        }
    }
    public record SlotView(Long id, Long activityId, Instant startsAt, boolean available, long waitingCount) {}
    public record CreateBooking(@NotNull Long slotId, @NotBlank @Size(max = 80) String guestName) {}
    public record BookingView(UUID code, Long slotId, String guestName, Booking.Status status, Instant startsAt) {
        static BookingView from(Booking booking) {
            return new BookingView(booking.getId(), booking.getSlot().getId(), booking.getGuestName(), booking.getStatus(), booking.getSlot().getStartsAt());
        }
    }
}
