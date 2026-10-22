package dev.joaolira.filaviva;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "slots")
public class Slot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    private Instant startsAt;

    protected Slot() {}
    public Slot(Activity activity, Instant startsAt) {
        this.activity = activity;
        this.startsAt = startsAt;
    }
    public Long getId() { return id; }
    public Activity getActivity() { return activity; }
    public Instant getStartsAt() { return startsAt; }
}
