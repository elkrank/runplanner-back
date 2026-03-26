# RunPlan Back API

API REST Spring Boot (Java 25 LTS) pour gérer:
- profil utilisateur
- sessions d'entraînement
- sommeil
- poids
- stats hebdomadaires

## Démarrage local (sans Docker)

```bash
mvn spring-boot:run
```

Base URL locale: `http://localhost:3000/v1`

OpenAPI JSON: `http://localhost:3000/v1/openapi`
Swagger UI: `http://localhost:3000/v1/swagger-ui`

## Démarrage avec Docker Compose (app + PostgreSQL)

```bash
docker compose up --build -d
```

Arrêt:

```bash
docker compose down
```

La base PostgreSQL est exposée sur `localhost:5432`.
