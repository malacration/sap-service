# AGENTS.md — sap-service

Spring Boot (Kotlin) middleware para integração com SAP B1 Service Layer.

---

## Stack

- Kotlin / Spring Boot 3.x / JDK 21
- Gradle (build/test)
- OkHttp3 (HTTP client)
- Caffeine (cache)
- JWT + OTP (auth)

---

## Comandos obrigatórios

Sempre execute após mudanças:

1. Build:
   ./gradlew build

2. Testes:
   ./gradlew test

3. Lint/check:
   ./gradlew check

---

## Estrutura do projeto (onde implementar)

- controllers/ → endpoints REST (sem lógica de negócio)
- services/ → lógica de negócio principal
- model/ → entidades/domínio
- infrastructure/ → integrações externas (SAP, HTTP, JWT, SOAP)
- events/ → comunicação entre domínios
- schedules/ → jobs agendados

---

## Regras obrigatórias

- NÃO implementar lógica de negócio em controllers
- SEMPRE colocar regras de negócio em services
- USAR apenas OkHttp3 para HTTP
- NÃO usar WebClient ou RestTemplate
- NÃO acessar SAP diretamente — usar infraestrutura existente
- SEMPRE mockar integrações externas em testes
- NÃO depender de serviços externos em testes unitários

---

## SAP Service Layer

- Todas chamadas ao SAP devem usar o client existente
- Sessão SAP (cookies/login) já é gerenciada — não reimplementar
- Endpoint padrão:
  https://10.117.163.230:50000

---

## Eventos (Spring)

- Use ApplicationEvent para comunicação entre domínios
- NÃO chamar diretamente serviços de outros domínios

---

## Schedules (jobs)

- Garantir idempotência
- Verificar concorrência (jobs podem rodar simultaneamente)

---

## Multi-filial

- NÃO assumir valores fixos
- Sempre considerar configuração por filial (properties ou headers)

---

## SQL Views

- Estão em: resources/views/
- Antes de alterar, verificar impacto em outros serviços

---

## Feature Flags

- Definidas em application.properties
- Antes de criar nova integração:
    - verificar se já existe flag
    - criar flag se necessário

---

## Testes

- Focar na camada de service
- Controllers não precisam de cobertura
- Testar eventos validando publicação, não efeitos colaterais

---

## Logs e erros

- Usar SLF4J
- Logar integrações externas
- Usar exceções tipadas (não usar Exception genérica)

---

## Armadilhas comuns

- Sessão SAP expirada → usar client existente
- Views SQL lentas → usar cache (Caffeine)
- PIX → garantir idempotência
- Feature flags podem estar desativadas em dev
- Certificado SAP pode exigir truststore

---

## Fluxo esperado de desenvolvimento

1. Implementar em services
2. Expor via controller (se necessário)
3. Adicionar testes
4. Validar com ./gradlew check