package dev.joaolira.filaviva;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    public enum Status { CONFIRMED, WAITLISTED, CANCELLED }
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;
    private String guestName;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    protected Booking() {}
    public Booking(Slot slot, String guestName, Status status) {
        this.id = UUID.randomUUID();
        this.slot = slot;
        this.guestName = guestName;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }
    public UUID getId() { return id; }
    public Slot getSlot() { return slot; }
    public String getGuestName() { return guestName; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void changeStatus(Status status) { this.status = status; this.updatedAt = Instant.now(); }
}
