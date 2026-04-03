# AI Platform Workspace

This repository now uses a separated frontend and backend setup:

- `frontend/`: Vue 3 + Vite SPA
- `src/main/java` and `src/main/resources`: Spring Boot backend and API

## Documentation

- 中文使用文档: [docs/使用文档.md](C:/Users/luoyn/Desktop/aiset/docs/使用文档.md)

## Local development

### 1. Prepare backend config

Copy the example file and fill in your local values:

```powershell
Copy-Item application-local.example.yml application-local.yml
```

Key items to configure:

- MySQL connection
- Redis connection if your local environment does not use defaults
- `aliyun.api-key` or proxy settings
- `app.storage.upload-dir` if you want uploads stored outside `./data/uploads`

### 2. Start the backend

```powershell
mvn spring-boot:run
```

Backend defaults:

- Base URL: `http://localhost:8080`
- API prefix: `http://localhost:8080/api`
- Uploaded files: `http://localhost:8080/uploads/**`

### 3. Start the frontend

```powershell
Set-Location frontend
npm install
npm run dev
```

Frontend defaults:

- Dev URL: `http://localhost:5173`
- Proxies:
  - `/api` -> `http://localhost:8080`
  - `/uploads` -> `http://localhost:8080`

## Production deployment

### Recommended deployment model

Deploy frontend and backend separately:

1. Build the frontend

```powershell
Set-Location frontend
npm install
npm run build
```

Output directory:

- `frontend/dist`

Deploy this directory to any static hosting service or reverse proxy.

2. Build the backend

```powershell
Set-Location ..
mvn -DskipTests package
```

Start the backend jar after packaging:

```powershell
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Reverse proxy notes

Typical routing:

- `/` -> frontend static site
- `/api` -> Spring Boot backend
- `/uploads` -> Spring Boot backend

Example Nginx config:

- `deploy/nginx.ai-platform.conf.example`

## Current structure notes

- `src/main/resources/static` is intentionally reduced to a minimal backend landing page.
- The backend no longer hosts the SPA bundle.
- Legacy frontend compatibility pages now live under `frontend/public`.

## Verification commands

Frontend build:

```powershell
Set-Location frontend
npm run build
```

Backend compile:

```powershell
Set-Location ..
mvn -DskipTests compile
```
