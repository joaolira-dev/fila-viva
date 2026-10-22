CREATE TABLE activities (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(300) NOT NULL,
  duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0)
);

CREATE TABLE slots (
  id BIGSERIAL PRIMARY KEY,
  activity_id BIGINT NOT NULL REFERENCES activities(id),
  starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT uq_slot_activity_time UNIQUE (activity_id, starts_at)
);

CREATE TABLE bookings (
  id UUID PRIMARY KEY,
  slot_id BIGINT NOT NULL REFERENCES slots(id),
  guest_name VARCHAR(80) NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED')),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_confirmed_booking_per_slot ON bookings(slot_id) WHERE status = 'CONFIRMED';
CREATE INDEX ix_bookings_waitlist ON bookings(slot_id, created_at, id) WHERE status = 'WAITLISTED';

INSERT INTO activities (name, description, duration_minutes) VALUES
  ('Consultoria rápida', 'Uma conversa para planejar os próximos passos.', 45),
  ('Revisão de projeto', 'Análise colaborativa de uma ideia ou entrega.', 60);
