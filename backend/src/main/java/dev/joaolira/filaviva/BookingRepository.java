package dev.joaolira.filaviva;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("select b.slot.id from Booking b where b.id = :id")
    Optional<Long> findSlotId(@Param("id") UUID id);
    boolean existsBySlotIdAndStatus(Long slotId, Booking.Status status);
    long countBySlotIdAndStatus(Long slotId, Booking.Status status);
    Optional<Booking> findFirstBySlotIdAndStatusOrderByCreatedAtAscIdAsc(Long slotId, Booking.Status status);
}
