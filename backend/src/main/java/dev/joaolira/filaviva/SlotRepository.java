package dev.joaolira.filaviva;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SlotRepository extends JpaRepository<Slot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findForUpdate(@Param("id") Long id);

    List<Slot> findByStartsAtBetweenOrderByStartsAtAsc(Instant from, Instant to);
    boolean existsByActivityIdAndStartsAt(Long activityId, Instant startsAt);
}
