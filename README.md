# Fila Viva

Plataforma de agendamentos com lista de espera. Projeto de estudo full stack, com evolução planejada para infraestrutura na AWS.

## Objetivo do produto

Uma pessoa escolhe uma atividade e um horário. A primeira reserva é confirmada; as seguintes entram na fila. Quando a reserva confirmada é cancelada, a primeira pessoa da fila assume a vaga.

## Arquitetura inicial

- `backend/`: Java 21, Spring Boot, PostgreSQL e Flyway.
- `frontend/`: React e TypeScript. A interface ainda está em construção.
- A aplicação começa como um serviço único para validar regras de negócio. Mensageria e implantação na AWS entram depois, quando houver um fluxo funcional e testado.

## Estado atual

O esquema do banco e as entidades da agenda estão implementados. A API de reservas e a interface ainda precisam de validação integrada.

## Banco local

Execute `docker compose up -d postgres` na raiz do projeto. A instância de desenvolvimento fica disponível apenas em `localhost:55433`. As credenciais em `compose.yaml` são exclusivamente locais; use outras credenciais em qualquer ambiente publicado.
