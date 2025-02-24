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

- [JDK 19+](http://www.oracle.com/technetwork/java)

## Para executar localmente

```shell
./gradlew install
./gradlew bootRun
```
