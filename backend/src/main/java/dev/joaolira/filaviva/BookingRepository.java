package dev.joaolira.filaviva;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsBySlotIdAndStatus(Long slotId, Booking.Status status);
    long countBySlotIdAndStatus(Long slotId, Booking.Status status);
    Optional<Booking> findFirstBySlotIdAndStatusOrderByCreatedAtAscIdAsc(Long slotId, Booking.Status status);
}
