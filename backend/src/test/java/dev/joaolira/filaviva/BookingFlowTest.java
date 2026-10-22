package dev.joaolira.filaviva;

import static org.assertj.core.api.Assertions.assertThat;

import dev.joaolira.filaviva.ApiDtos.BookingView;
import dev.joaolira.filaviva.ApiDtos.CreateBooking;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class BookingFlowTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired BookingService service;
    @Autowired SlotRepository slots;

    @Test
    void concurrentBookingsCreateOneConfirmedAndPromoteFirstWaitingAfterCancellation() throws Exception {
        Long slotId = slots.findByStartsAtBetweenOrderByStartsAtAsc(Instant.now(), Instant.now().plusSeconds(86400 * 14))
            .getFirst().getId();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<BookingView> first = executor.submit(() -> {
                start.await();
                return service.create(new CreateBooking(slotId, "Pessoa A"));
            });
            Future<BookingView> second = executor.submit(() -> {
                start.await();
                return service.create(new CreateBooking(slotId, "Pessoa B"));
            });
            start.countDown();
            BookingView a = first.get();
            BookingView b = second.get();
            assertThat(a.status()).isNotEqualTo(b.status());
            BookingView confirmed = a.status() == Booking.Status.CONFIRMED ? a : b;
            BookingView waiting = a.status() == Booking.Status.WAITLISTED ? a : b;
            assertThat(service.cancel(confirmed.code()).status()).isEqualTo(Booking.Status.CANCELLED);
            assertThat(service.get(waiting.code()).status()).isEqualTo(Booking.Status.CONFIRMED);
        }
    }
}
