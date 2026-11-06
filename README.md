# Gestão de Lixo Eletrônico

APP para controle, rastreabilidade e balanço de massa no descarte e reciclagem de resíduos eletrônicos.

Projeto desenvolvido para a disciplina de **Processo de Desenvolvimento de Software (PDS)** do curso de Sistemas de Informação da Universidade Federal de Uberlândia (UFU).

---

## Equipe

* Gianluca Justino Agostinho
* João Victor Barbosa Marques
* Pietro Alves Zanotim
* Schumacher Itagiba Gonçalves

---

## Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 4.x (Spring Web, Spring Data JPA, Spring Validation)
* **Banco de Dados:** PostgreSQL
* **Utilitários:** Lombok
* **Gerenciador de Dependências:** Maven (com Maven Wrapper incluído)

---

## Como Executar o Projeto

### Pré-requisitos
* **Java 17** ou superior instalado.
* **PostgreSQL** instalado e em execução na máquina local (porta padrão `5432`).
* Git configurado.

### 1. Criar o Banco de Dados
Abra o **pgAdmin**, **DBeaver** ou o terminal `psql` e execute o comando:

```sql
CREATE DATABASE lixo_eletronico_db;
```

> **Atenção:** O Spring Data JPA / Hibernate criará e atualizará as tabelas automaticamente (`ddl-auto: update`), mas a base `lixo_eletronico_db` precisa existir previamente.

### 2. Configurar Credenciais do PostgreSQL (se necessário)
Por padrão, o projeto conecta usando:
* **Host:** `localhost`
* **Porta:** `5432`
* **Usuário:** `postgres`
* **Senha:** `postgres`

Se a sua senha ou usuário local do PostgreSQL forem diferentes, você pode:
- **Opção A (Variáveis de Ambiente):** Definir as variáveis `DB_USER` e `DB_PASSWORD` no seu sistema ou na configuração de execução da IDE.
- **Opção B (Arquivo Local - Recomendado):** Criar um arquivo `application-local.yaml` dentro de `src/main/resources/` (esse arquivo já está no `.gitignore`):

```yaml
spring:
  datasource:
    username: seu_usuario
    password: sua_senha
```

### 3. Executar a Aplicação
Pelo terminal na raiz do projeto:

* **Windows:**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **Linux / Mac:**
  ```bash
  ./mvnw spring-boot:run
  ```
*(Ou execute a classe principal `LixoEletronicoApplication.java` diretamente pela sua IDE preferida: VS Code, IntelliJ, Eclipse).*

### 4. Validar se a Aplicação Subiu
Acesse no seu navegador ou cliente HTTP (Postman/Insomnia):

**URL:** `GET http://localhost:8080/api/status`

**Resposta esperada (JSON):**
```json
{
  "status": "operacional",
  "projeto": "gestao-lixo-eletronico"
}
```
