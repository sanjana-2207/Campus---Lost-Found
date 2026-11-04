# REC Lost & Found — Web App

Spring Boot + Thymeleaf web version of REC Lost & Found. The demo uses an
in-memory H2 database, so data resets whenever the app restarts.

## Demo accounts

| Role | E-mail | Password |
|---|---|---|
| Student | arjun@campus.edu | student123 |
| Staff | security@campus.edu | staff123 |
| Admin | admin@campus.edu | admin123 |

## Run locally

```bash
cd web
mvn spring-boot:run
```

Open http://localhost:8080.

## Features

- Report lost items and found items with photos, category, location and date.
- Browse and filter lost reports and claimable found items.
- Submit ownership claims, verify them as staff, and confirm handovers.
- Track claim notifications, personal reports and recovery analytics.
- Export the full item list as CSV.

## Deploy to Render

1. Push this repository to GitHub.
2. In Render, choose **New > Blueprint** and select the repository. The root
   `render.yaml` builds the project with the root `Dockerfile`.
3. Alternatively, create a **Web Service** manually:
   - Runtime: Docker
   - Dockerfile path: `./Dockerfile` (repository root)
4. Render provides the `PORT` environment variable; the app binds to it. The
   `/health` endpoint is public and is used as the Render health check.
5. The H2 console is off by default. Set `H2_CONSOLE=true` only in a trusted
   local environment.

The H2 database is in-memory and resets on every deploy or restart. To keep
data permanently, replace the H2 datasource in
`src/main/resources/application.properties` with a hosted MySQL/Postgres
database and add the matching JDBC driver.
