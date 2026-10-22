# Roteiro de evolução

## Etapa 1: núcleo funcional — concluída localmente

- Modelo de atividades, horários e reservas com migração versionada.
- Reserva concorrente, fila de espera e promoção após cancelamento.
- Interface responsiva para escolher, reservar, consultar e cancelar.
- Teste de integração com PostgreSQL real via Testcontainers.

## Etapa 2: qualidade e operação

- Pipeline de build e teste para backend e frontend no GitHub Actions.
- OpenAPI, respostas de erro padronizadas e testes dos contratos HTTP.
- Separar dados demonstrativos da configuração de produção.
- Paginar consultas e medir o tempo de resposta da agenda.

## Etapa 3: AWS

- Criar imagem Docker da API e publicá-la no ECR.
- Executar a API em ECS/Fargate e o PostgreSQL em RDS privado.
- Publicar o frontend estático com origem HTTPS e configurar CORS somente para essa origem.
- Usar IAM com permissões mínimas, segredos fora do repositório e logs no CloudWatch.
- Descrever os recursos com infraestrutura como código e automatizar o deploy.
- Estimar custo na região escolhida e configurar alertas antes de deixar recursos ativos.

## Etapa 4: eventos e produto

- Enviar um evento quando alguém for promovido na fila; processar notificações de forma idempotente.
- Adicionar contas e papéis para clientes e operadores, com autorização em cada operação.
- Criar um painel para configurar atividades, disponibilidade e regras de cancelamento.

Mensageria e um segundo serviço só entram quando as notificações estiverem definidas. O objetivo é mostrar por que a separação ajuda, e não criar microsserviços sem uma necessidade do produto.
