# SAP-SERVICE
Middleware para o SAP B1 Service Layer

Atualmente o projeto utiliza a stack do [spring boot](https://spring.io/).

## Como executar o projeto em produção

A maneira mais facil de se executar o projeto é utilizando docker.
O mais recomendado é com o docker compose - [Link de instação](https://docs.docker.com/compose/install/).


### Instruções basicas de como utilizar o docker compose

Para uma inicialização com bloqueio do terminal
```shell
docker compose up
```
Adicione o parameto -d para colocar o processo em segundo plano
```shell
docker compose up -d
```
Para verificar os logs da aplicação. O parameto -f colocar os logs em streamer no terminal. o parametro --tail=100 faz a leitura das 100 linhas a partir de EOF
```shell
docker compose logs
```

### Instruções basicas de como utilizar o docker.

```shell
docker run malacration/sap-service
```

## Requirements

Para building e execução é necessario:

- JDK 25 LTS (build e execução), Kotlin 2.4.21 e Gradle 9.8.2 pelo wrapper.

## Para executar localmente

```shell
./gradlew test bootJar
./gradlew bootRun
```

## Compatibilidade da atualização

Spring Boot permanece em 3.5.16 para preservar os contratos Jackson 2 e a integração SOAP Axis/SoftExpert. A migração para Boot 4 exige uma revisão separada dessas integrações; a linha 3.5 já encerrou o suporte comunitário. Bibliotecas legadas dessa integração permanecem como exceção conhecida.

A validação local da atualização executou 1.354 testes sem falhas e gerou o JAR com Java 25. Operações em SAP e SoftExpert reais precisam de homologação com esses serviços.
