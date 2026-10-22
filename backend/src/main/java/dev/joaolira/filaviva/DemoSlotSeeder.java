package dev.joaolira.filaviva;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoSlotSeeder implements CommandLineRunner {
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private final ActivityRepository activities;
    private final SlotRepository slots;

    DemoSlotSeeder(ActivityRepository activities, SlotRepository slots) {
        this.activities = activities;
        this.slots = slots;
    }

    @Override
    public void run(String... args) {
        for (Activity activity : activities.findAll()) {
            for (int day = 1; day <= 14; day++) {
                LocalDate date = LocalDate.now(ZONE).plusDays(day);
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                for (LocalTime time : new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(14, 0)}) {
                    var instant = date.atTime(time).atZone(ZONE).toInstant();
                    if (!slots.existsByActivityIdAndStartsAt(activity.getId(), instant)) {
                        slots.save(new Slot(activity, instant));
                    }
                }
            }
        }
    }
}
