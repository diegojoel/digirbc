# DigiRBC

Sistema web de help desk com Raciocínio Baseado em Casos (RBC) para suporte a sistemas de automação comercial. A descrição do problema é um conjunto de atributos com peso; a busca compara o problema novo com os casos do módulo e devolve as soluções mais parecidas.

O sistema segue o artigo [ArtigoCientifico.pdf](ArtigoCientifico.pdf), *Aplicação de Raciocínio Baseado em Casos no suporte a decisão de um sistema web de help desk* (Diego Joel Pilger e Marcel Hugo, FURB, 2012). O PDF fica neste diretório, ao lado deste arquivo.

## Como acessar

Requisito: Java 11.

```bat
mvnw.cmd spring-boot:run
```

Abra [http://localhost:8080/login.xhtml](http://localhost:8080/login.xhtml).

Se a porta 8080 estiver ocupada:

```bat
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8091"
```

Nesse caso o endereço é [http://localhost:8091/login.xhtml](http://localhost:8091/login.xhtml).

| Login    | Senha    | Perfil                      |
|----------|----------|-----------------------------|
| admin    | admin    | Administrador do sistema    |
| tecnico  | tecnico  | Suporte técnico             |
| cliente  | cliente  | Cliente (usuário final)     |

O banco padrão é o H2, em arquivo (`./data/digirbc`). A aplicação sobe nele, sem servidor.

O PostgreSQL já está configurado (`localhost:5432/digirbc`, usuário e senha `postgres`). Para usá-lo, mude o profile no `application.properties`:

```
spring.profiles.active=postgres
```

Na primeira subida, o banco vazio recebe os perfis, os usuários, os módulos e a base de casos.
