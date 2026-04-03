# Frontend

This directory contains the Vue 3 frontend migrated to a standard Vite project.

## Stack

- Vue 3
- Vue Router 4
- JavaScript
- Vite

## Commands

```bash
npm install
npm run dev
npm run build
```

## Dev server

`vite.config.js` proxies these paths to the Spring Boot backend running on `http://localhost:8080`:

- `/api`
- `/uploads`

Static files under `frontend/public` are served directly by Vite during development.

## Build output

Production build output is written to:

`./dist`

The backend no longer serves the SPA bundle from `src/main/resources/static`. The `resources/static` directory is now kept as a minimal backend landing page only.

## Routing

The SPA uses hash routing:

- `/#/`
- `/#/history`
- `/#/sql-detail?id=...`
- `/#/image`
- `/#/camera`
- `/#/video`
- `/#/voice`
- `/#/login`
- `/#/register`

Compatibility redirect pages are emitted from `frontend/public` for legacy URLs such as:

- `/login.html`
- `/register.html`
- `/history.html`
- `/image.html`
- `/3d-camera.html`
- `/video.html`
- `/voice.html`
- `/sql-detail.html?id=...`
