# Fila Viva

Plataforma de agendamentos com lista de espera. Projeto de estudo full stack, com evolução planejada para infraestrutura na AWS.

## Objetivo do produto

Uma pessoa escolhe uma atividade e um horário. A primeira reserva é confirmada; as seguintes entram na fila. Quando a reserva confirmada é cancelada, a primeira pessoa da fila assume a vaga.

## Arquitetura inicial

- `backend/`: Java 21, Spring Boot, PostgreSQL e Flyway.
- `frontend/`: React, TypeScript e Vite.
- A aplicação começa como um serviço único para validar regras de negócio. Mensageria e implantação na AWS entram depois, quando houver um fluxo funcional e testado.

## Estado atual

A API e a interface de agendamento estão funcionais localmente. O fluxo de reserva, fila e cancelamento foi validado com PostgreSQL e no navegador. AWS, autenticação de contas e notificações ficam para as próximas etapas.

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

## Interface local

Com o banco e a API em execução, rode `npm install` e `npm run dev` dentro de `frontend/`. Abra `http://localhost:5173`. O Vite encaminha as chamadas `/api` para `http://localhost:18080`. Em uma publicação com origens separadas, defina `VITE_API_URL` no build da interface e `CORS_ORIGIN` na API.

Os horários de demonstração são gerados para os próximos 14 dias úteis, no fuso de Brasília. A interface guarda o código da reserva apenas no armazenamento local do navegador para permitir consulta e cancelamento.
