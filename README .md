# StaffForge – Resource & Talent Allocation Platform

**StaffForge** is an internal enterprise platform designed for Project Managers (PMs), Resource Managers, and HR departments. It replaces static spreadsheets with a dynamic system that tracks engineer skills, experience levels, and availability to assemble optimal project teams.

---

## 1. Tech Stack & Version Standards

To prevent environment mismatches and merge conflicts, all team members **must** use the following versions locally:

### Backend
* **Language:** Java 21 LTS
* **Framework:** Spring Boot 3.3+
* **Security:** Spring Security 6 (Stateless JWT Authentication)
* **Build Tool:** Maven (use the included `./mvnw` wrapper)
* **Database:** PostgreSQL 16
* **Database Migrations:** Flyway (plain SQL versioned migrations: `V1__...sql`)

### Frontend
* **Runtime:** Node.js LTS (v20+)
* **Framework:** Angular 18+
* **Architecture:** **Standalone Components** (`standalone: true` by default — **do not** generate traditional `NgModules`)
* **UI Library:** Angular Material / Tailwind CSS *(Lock in team choice before UI-1)*

### DevOps & Local Environment
* **Containerization:** Docker & Docker Compose

---

## 2. Port Conventions

Keep these ports free when running the stack locally or via Docker Compose:

| Service | Local Port | Base URL / Connection |
| :--- | :--- | :--- |
| **PostgreSQL 16 (Database)** | `5432` | `jdbc:postgresql://localhost:5432/staffforge` |
| **Spring Boot (Backend API)** | `8080` | `http://localhost:8080/api` |
| **Angular (Frontend UI)** | `4200` | `http://localhost:4200` |

---

## 3. Monorepo Directory Structure

All services live in this single repository:

```text
staff-forge/
├── backend/                # Spring Boot 3.3+ project (Java 21, Maven)
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── db/migration/   # Flyway SQL scripts (V1__init.sql, etc.)
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/               # Angular 18+ project (Standalone Components)
│   ├── src/app/
│   │   ├── core/           # Guards, Interceptors, Auth & API Services
│   │   ├── shared/         # Navbar, Sidebar, reusable UI components
│   │   └── features/       # Role-specific pages & dashboards
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml      # Orchestrates PostgreSQL, Backend, and Frontend
└── README.md
```

---

## 4. Team Workflow, Git & Jira Rules

### Picking Tasks (Jira)
1. Pick **1 unblocked, unassigned task** from the current Sprint's `To Do` column.
2. Assign it to yourself and move it to **`In Progress`**.
3. Keep a maximum of **1–2 tasks** in `In Progress` at any time.

### Branching Strategy
* **`main`**: Production-ready code (end-of-sprint stable releases).
* **`develop`**: Default integration branch. **Branch off `develop` and open Pull Requests back into `develop`.**
* **Feature Branch Naming:** Always include the Jira ticket key in your branch name:
  ```bash
  git checkout develop
  git pull origin develop
  git checkout -b feature/SF-<ticket-number>-short-description
  # Example: feature/SF-7-jwt-filter
  ```

### Definition of Done (DoD) & Pull Requests
A Jira ticket can only be moved to **`Done`** when:
1. The code compiles cleanly and passes local testing.
2. Any database schema changes are placed in a new **Flyway migration file** (`V<number>__description.sql`) — never modify an already merged migration file.
3. A Pull Request (PR) is opened against `develop` and the Jira ticket is moved to **`In Review`**.
4. The PR is merged into `develop` without breaking the `docker-compose` build.

---

## 5. Quick Start (Local Development)

### Start Database Only (For Local IDE Development)
```bash
docker compose up -d postgres
```

### Run Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```

### Run Frontend (Angular)
```bash
cd frontend
npm install
npm start
```

### Run Full Stack via Docker
```bash
docker compose up --build
```