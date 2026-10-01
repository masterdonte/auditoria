# Auditoria API

API de auditoria desenvolvida com Java 17 e Spring Boot, com autenticação via LDAP/Active Directory, autorização por perfis e JWT, persistência em PostgreSQL e consultas ao ClickHouse.

## Tecnologias principais

- Java 17
- Spring Boot 4.1.1
- Spring Security / LDAP
- Spring Data JPA / Hibernate
- PostgreSQL 16.8
- ClickHouse JDBC
- JWT (JJWT)
- Apache POI
- Maven
- Docker / Docker Compose

## Pré-requisitos

Para executar localmente pelo IntelliJ: JDK 17, IntelliJ IDEA, Docker/Docker Compose e acesso de rede aos serviços LDAP e ClickHouse configurados pela aplicação.

Para executar tudo via Docker Compose: Docker/Docker Compose e acesso de rede aos serviços LDAP e ClickHouse.

## Variáveis de ambiente

| Variável | Finalidade |
| --- | --- |
| `JWT_SECRET` | Chave de assinatura dos tokens JWT |
| `CLICK_USER` | Usuário do ClickHouse |
| `CLICK_PASS` | Senha do ClickHouse |
| `POST_URL` | URL JDBC do PostgreSQL |
| `POST_USER` | Usuário do PostgreSQL |
| `POST_PASS` | Senha do PostgreSQL |
| `LDAP_PASS` | Senha da conta usada para consulta ao LDAP |

Não versione credenciais reais. O `.gitignore` ignora `.env` e `.env.*`, exceto `.env.example`.

## Execução local pelo IntelliJ

Neste modo, somente o PostgreSQL é executado pelo Docker. A API é iniciada pelo IntelliJ.

### 1. Subir somente o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d postgres
```

O PostgreSQL ficará disponível pela porta `5432` da máquina local.

### 2. Configurar as variáveis no IntelliJ

Abra **Run > Edit Configurations > configuração da aplicação > Environment variables** e configure:

```text
JWT_SECRET=<chave-jwt>
CLICK_USER=<usuario-clickhouse>
CLICK_PASS=<senha-clickhouse>
POST_URL=jdbc:postgresql://localhost:5432/auditoria
POST_USER=postgres
POST_PASS=<senha-postgres>
LDAP_PASS=<senha-ldap>
```

Neste modo o host do PostgreSQL é `localhost`, pois a aplicação roda na máquina host e acessa a porta publicada pelo container.

### 3. Profile da aplicação

O `application.properties` define o profile Spring a partir do profile Maven:

```properties
spring.profiles.active=@activatedProperties@
```

O `pom.xml` possui atualmente os profiles Maven `homo` (ativo por padrão) e `prod`. O profile `homo` utiliza `application-homo.properties`.

### 4. Executar

Execute a classe principal da aplicação pelo IntelliJ. A API utiliza a porta 80:

```text
http://localhost:80
```

## Execução completa via Docker Compose

Neste modo, PostgreSQL e API são executados em containers.

### 1. Criar o arquivo `.env`

Crie `.env` na mesma pasta do `docker-compose.yml`:

```dotenv
JWT_SECRET=<chave-jwt>
CLICK_USER=<usuario-clickhouse>
CLICK_PASS=<senha-clickhouse>
POST_URL=jdbc:postgresql://postgres:5432/auditoria
POST_USER=postgres
POST_PASS=<senha-postgres>
LDAP_PASS=<senha-ldap>
```

O `.env` não deve ser commitado. No Compose, `postgres` é o hostname do banco porque é o nome do serviço.

### 2. Construir e iniciar

```bash
docker compose up -d --build
```

O build atual da API utiliza o profile Maven `homo`.

### 3. Logs

API:

```bash
docker compose logs -f auditoria
```

PostgreSQL:

```bash
docker compose logs -f postgres
```

### 4. Parar os serviços

```bash
docker compose stop
```

### 5. Remover containers preservando o banco

```bash
docker compose down
```

O volume do PostgreSQL é preservado.

## IntelliJ x Docker Compose

A principal diferença é a URL do PostgreSQL.

Aplicação pelo IntelliJ:

```text
POST_URL=jdbc:postgresql://localhost:5432/auditoria
```

Aplicação pelo Docker Compose:

```text
POST_URL=jdbc:postgresql://postgres:5432/auditoria
```

Dentro do container da API, `localhost` apontaria para o próprio container da API, e não para o PostgreSQL. Por isso o Compose utiliza `postgres`.

## Inicialização do PostgreSQL

O Compose monta `./docker/postgres/init` em `/docker-entrypoint-initdb.d`.

Atualmente os scripts são executados em ordem alfabética:

```text
01-auditoria_ddl.sql
02-audit_legado.sql
03-other-data.sql
```

Eles criam a estrutura e carregam os dados iniciais utilizados no ambiente Docker.

### Reexecutar os scripts desde o início

Os scripts de `/docker-entrypoint-initdb.d` são executados quando o diretório de dados do PostgreSQL está vazio. Como o projeto utiliza o volume `pgdata`, simplesmente derrubar e subir os containers não recria o banco.

Para apagar completamente o banco local e executar novamente os scripts:

```bash
docker compose down -v
docker compose up -d --build
```

**Atenção:** `down -v` remove o volume e apaga os dados do PostgreSQL desse ambiente.

## Comandos úteis

Subir somente o banco:

```bash
docker compose up -d postgres
```

Rebuild normal da API após alterações no código:

```bash
docker compose up -d --build auditoria
```

Build sem cache, somente quando for necessário descartar completamente o cache do Docker:

```bash
docker compose build --no-cache auditoria
docker compose up -d auditoria
```

Não é necessário usar `--no-cache` no fluxo normal de desenvolvimento.

Ver containers:

```bash
docker compose ps
```

Reiniciar a API:

```bash
docker compose restart auditoria
```

## Dockerfile

O build utiliza duas etapas. Na primeira, `maven:3.9-eclipse-temurin-17` compila a aplicação. As dependências Maven são preparadas antes da cópia do código-fonte para permitir reaproveitamento do cache do Docker.

Na segunda etapa, a aplicação é executada com `eclipse-temurin:17-jre`. O JAR gerado é copiado para a imagem final como `app.jar`.

## Profiles Maven

O `pom.xml` possui os profiles `homo` e `prod`. No Docker Compose atual:

```yaml
build:
  args:
    MAVEN_PROFILE: homo
```

O Dockerfile utiliza esse valor em:

```bash
mvn clean package -P${MAVEN_PROFILE} -DskipTests
```

O profile Maven define o valor usado por `spring.profiles.active=@activatedProperties@`.

## Segurança

A aplicação utiliza LDAP/Active Directory para autenticação, usuários/perfis locais para controle de acesso e JWT para autenticação das requisições após o login.

Credenciais e a chave JWT devem ser fornecidas por variáveis de ambiente e nunca armazenadas diretamente no repositório.

## Arquivos de configuração

Configurações compartilhadas:

```text
src/main/resources/application.properties
```

Homologação:

```text
src/main/resources/application-homo.properties
```

Produção:

```text
src/main/resources/application-prod.properties
```

O datasource do profile `homo` utiliza:

```properties
spring.datasource.url=${POST_URL}
spring.datasource.username=${POST_USER}
spring.datasource.password=${POST_PASS}
```

Isso permite usar a mesma configuração no IntelliJ e no Docker, alterando apenas as variáveis de ambiente.

## Gerar o arquivo de dump do banco de dados a partir do container
```bash
docker exec -t postgres_16_8 pg_dump -U postgres -d auditoria --schema-only --no-owner --no-privileges > auditoria_ddl.sql
```
