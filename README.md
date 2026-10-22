# Fila Viva

Plataforma de agendamentos com lista de espera. Projeto de estudo full stack, com evolução planejada para infraestrutura na AWS.

## Objetivo do produto

Uma pessoa escolhe uma atividade e um horário. A primeira reserva é confirmada; as seguintes entram na fila. Quando a reserva confirmada é cancelada, a primeira pessoa da fila assume a vaga.

## Arquitetura inicial

- `backend/`: Java 21, Spring Boot, PostgreSQL e Flyway.
- `frontend/`: React e TypeScript. A interface ainda está em construção.
- A aplicação começa como um serviço único para validar regras de negócio. Mensageria e implantação na AWS entram depois, quando houver um fluxo funcional e testado.

## Estado atual

A API de reservas está funcional e foi validada contra PostgreSQL. A interface React ainda está em construção.

## Banco local

Execute `docker compose up -d postgres` na raiz do projeto. A instância de desenvolvimento fica disponível apenas em `localhost:55433`. As credenciais em `compose.yaml` são exclusivamente locais; use outras credenciais em qualquer ambiente publicado.

## API

Depois de iniciar o banco, execute `mvn spring-boot:run` dentro de `backend/`. A API responde em `http://localhost:18080`.

| Método | Rota | Finalidade |
| --- | --- | --- |
| `GET` | `/api/activities` | Listar tipos de atendimento |
| `GET` | `/api/slots` | Listar horários dos próximos 14 dias |
| `POST` | `/api/bookings` | Reservar horário ou entrar na fila |
| `GET` | `/api/bookings/{code}` | Consultar uma reserva pelo código |
| `DELETE` | `/api/bookings/{code}` | Cancelar e promover a primeira pessoa da fila |

Exemplo de corpo para `POST /api/bookings`: `{"slotId": 1, "guestName": "Pessoa de teste"}`. O código retornado funciona como acesso à reserva; não o publique. Nesta fase demonstrativa não há contas de usuário nem notificações.
