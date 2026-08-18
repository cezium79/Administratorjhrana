

Да, конечно! Сначала让我 изучу структуру проекта и его содержимое, чтобы написать полноценный README.




I'll start by reading multiple files and listing directories in parallel to gather as much information as possible.











Good, I have the high-level picture. Now let me dig deeper into the backend Java structure and frontend files.








Excellent. Now let me read the key backend and frontend files to understand the business logic.











Now let me read the models, remaining services, and all frontend files.













Let me now read the remaining services, views, components, and other important files.













Let me read the remaining files - views, components, and remaining backend services.













Let me read the remaining DTOs and frontend CSS to complete the picture.











I have completed a thorough exploration of the entire project. Here is the complete analysis:

---

## PROJECT OVERVIEW: Administrator Jhrana

**Purpose:** A web application for managing security patrol reports, monitoring violations, and analyzing incidents. The system collects, stores, and processes guard patrol reports, generates statistics, and supports file storage (local and Yandex Cloud S3).

---

## STACK TECHNOLOGIES

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.0 | Main framework |
| Spring Security | bundled | Authentication/Authorization |
| Spring Data JPA | bundled | ORM/Hibernate |
| PostgreSQL | 14+ | Primary database |
| H2 | bundled | DB admin console |
| JWT (jjwt) | 0.12.5 | Authentication tokens |
| AWS SDK S3 | 2.25.60 | Yandex Cloud object storage |
| Lombok | 1.18.32 | Boilerplate code generation |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Vue.js | 3.4+ | UI framework |
| Vue Router | 4.2.5 | Routing |
| Pinia | 2.1.7 | State management |
| Axios | 1.6+ | HTTP client |
| Chart.js | 4.4+ | Data visualization |
| vue-chartjs | 5.2+ | Chart.js integration |
| XLSX | 0.18.5 | Excel export |
| Vite | 5.0+ | Build tool |

---

## BACKEND STRUCTURE

**Package:** `com.administratorjhrana`

### Application Entry Point
- **`AdministratorJhranaApplication.java`** -- Spring Boot main class with `@EnableScheduling`

### Config (7 files)
- **`SecurityConfig.java`** -- HTTP security, CORS, BCrypt password encoder, custom `UserDetailsService` (temporarily assigns `ROLE_ADMIN` to all users)
- **`JwtAuthenticationFilter.java`** -- `OncePerRequestFilter` that validates JWT tokens; whitelists `/api/reports/**`, `/reports/**`, `/h2-console/**` as public endpoints
- **`JwtService.java`** -- JWT token generation, validation, claim extraction using jjwt 0.12.5
- **`DataInitializer.java`** -- `CommandLineRunner` bean that creates an admin user on startup (username/password from `app.admin.*` properties)
- **`S3Config.java`** -- Creates `S3Client` bean pointing to `storage.yandexcloud.net` in region `ru-central1`, with bucket name bean
- **`WebConfig.java`** -- Additional CORS mapping allowing `localhost:5173` and `localhost:3000`
- **`CustomUserDetails.java`** -- `UserDetails` implementation wrapping `User` entity

### Controllers (5 files)
- **`AuthController`** (`/api/auth`) -- `/login` (JWT auth), `/refresh`, `/verify`
- **`ReportController`** (`/api/reports`) -- CRUD for reports: upload (multipart), upload from URL, mobile submission (JSON), list with pagination/filters, get by ID, update, delete, serve HTML/PDF files, download, send via email, check emails manually, get filter values
- **`StatisticsController`** (`/api/statistics`) -- `/violations` (count by type), `/checkpoints` (average checkpoint passing times)
- **`SearchController`** (`/api/reports`) -- `/search?query=` for report search

### Services (7 files)
- **`ReportService`** -- Core report service: saves reports from file, URL, or mobile DTO; creates Rounds, Violations, and CheckpointLogs from mobile submission data; pagination and filtering
- **`ReportProcessingService`** -- Processes mobile app submissions: parses `ReportSubmissionDTO`, creates Report/Round/CheckpointLog/Violation/Incident entities, downloads photos from S3
- **`ImapReportReceiver`** -- Email receiver: hardcoded IMAP credentials for `belkinnikola2@yandex.ru` on `imap.yandex.ru`, scans for unread emails from specific senders, saves attachments as reports. Scheduled every 30 minutes (`@Scheduled(fixedRate = 1800000)`)
- **`JwtService`** -- JWT token operations (create, validate, extract claims)
- **`StorageService`** -- Local file storage with allowed extensions: `.html`, `.htm`, `.pdf`, `.txt`, `.doc`, `.docx`
- **`S3StorageService`** -- Yandex Cloud S3 operations: list folders/files, download files, upload, move objects
- **`FileStorageService`** -- Saves photos with timestamp-based filenames to `uploads/photos/` directory
- **`EmailService`** -- Sends report attachments via email using `JavaMailSender`

### Scheduler
- **`ReportScheduler`** -- `@Scheduled(fixedDelay = 300000)` (every 5 minutes): scans S3 for new `reports/` folders, processes `report.json` files via `ReportProcessingService`, moves processed folders to `processed/`

### Models (6 entities)
- **`User`** -- Simple entity: id, username (unique), password (BCrypt)
- **`Report`** -- Main entity: id, title, guardName, date, endTime, htmlContent, pdfUrl, htmlUrl, filePath, sentUrl, size, uploadedAt, notes; has `List<Round>` children
- **`Round`** -- Linked to Report: roundNumber, location, startTime, endTime, filePath; has `List<Violation>` and `List<CheckpointLog>` children
- **`Violation`** -- Linked to Round: type, description, severity, imageUrls, detectedAt; has `List<Incident>` children; note: setters are overridden as no-ops (likely a bug)
- **`Incident`** -- Linked to Round/Violation: status, notes, assignedTo, resolvedAt, timestamp, incidentType, description, photoPath
- **`CheckpointLog`** -- Linked to Round: checkpointId, checkpointName, timestamp, routeName, sequenceIndex, isSequenceCorrect, scanType, actionType, sequenceErrorType, inputValue, photoPath, answer

### DTOs (8 files)
- **`LoginRequest`** -- username, password (with `@NotBlank` validation)
- **`ReportDTO`** -- title, guardName, date, notes
- **`ReportSubmissionDTO`** -- Mobile app input: shiftId, employeeName, startTime, endTime, strictSequenceEnabled, List of rounds/logs/violations/incidents
- **`RoundDTO`** -- roundId, startTime, endTime, routeId, routeName, checkpointsCount, checkpointsPassed, sequenceViolations
- **`LogDTO`** -- checkpointName/id, timestamp, roundId, routeName, sequenceIndex, isSequenceCorrect, scanType, actionType, photoPath, sequenceErrorType, inputValue, answer
- **`ViolationDTO`** -- type, description, severity, imageUrls, detectedAt, roundId
- **`IncidentDTO`** -- timestamp, shiftId, roundId, employeeName, incidentType, description, photoPath
- **`LogEntryDTO`** -- (exists but appears unused)

### Repositories (7 files)
- **`UserRepository`** -- findByUsername
- **`ReportRepository`** -- findAllByOrderByUploadedAtDesc, findByDateBetween, findByGuardNameContainingIgnoreCase, findByTitleContainingIgnoreCase, findDistinctGuardNames, findDistinctTitles
- **`RoundRepository`** -- standard JPA repository
- **`ViolationRepository`** -- standard JPA repository
- **`IncidentRepository`** -- standard JPA repository
- **`CheckpointLogRepository`** -- standard JPA repository
- **`StatisticsRepository`** -- Custom queries: countViolationsByType, getCheckpointPassingTimes

### Exception Handling
- **`GlobalExceptionHandler`** -- Handles RuntimeException (500), MaxUploadSizeExceededException (413), generic Exception (500)

---

## FRONTEND STRUCTURE

### API Layer
- **`frontend/src/api/http.js`** -- Axios instance with base URL `/api`, JWT interceptor (adds `Bearer` token), 401 interceptor (clears auth store and redirects to `/login`)

### State Management
- **`frontend/src/stores/auth.js`** -- Pinia store: token, username, isAuthenticated (computed), login() method, logout() method. Persists to localStorage

### Router
- **`frontend/src/router/index.js`** -- Two routes:
  - `/login` -- LoginView (requiresGuest)
  - `/` -- ReportsView (requiresAuth)
  - Navigation guards redirect based on auth state

### Views
- **`LoginView.vue`** -- Login form with username/password fields, error message, loading state. Calls authStore.login()
- **`ReportsView.vue`** -- Main dashboard:
  - Filters: guard name dropdown, title search, date range
  - Reports table with sortable columns (ID, date, guard, title, type badge, size)
  - Pagination (20 per page)
  - Actions: preview (opens modal), download, print, delete
  - Email modal for sending reports
  - "Check Emails" button that triggers `checkEmails` endpoint
  - Components: ReportTypeBadge, ReportPreviewModal
- **`StatisticsPage.vue`** -- Charts dashboard:
  - Line chart for violations by type
  - Bar chart for checkpoint passing times
  - Filters: guard, date range
  - Export to Excel/CSV using XLSX library

### Components
- **`ReportPreviewModal.vue`** -- Two-tab modal: Preview (iframe for HTML, embed for PDF) and Info (editable fields: title, guard name, date, notes). Footer buttons: save, close, download, email, print, delete
- **`ReportTypeBadge.vue`** -- Badge component showing report type: "PDF", "HTML", "HTML+PDF", or "Файл" with color-coded styles

### Assets
- **`main.css`** -- Global styles for the entire application (layout, buttons, tables, modals, forms, badges, pagination, spinner, login page)

### Main
- **`frontend/src/main.js`** -- Creates Vue app, mounts Pinia and Router, mounts to `#app`
- **`frontend/src/App.vue`** -- Simple `<router-view>` with global reset styles

---

## CONFIGURATION

### `application.yml` (src/main/resources/application.yml)
- Server port: 8080
- Imports secrets from `./config/application-secrets.yml`
- Datasource: `jdbc:postgresql://localhost:5440/jhrana_db`
- JPA: `ddl-auto: update`
- H2 console: enabled at `/h2-console`
- Multipart: max 10MB
- Mail: smtp.gmail.com:587 with STARTTLS
- S3: Yandex Cloud storage endpoint, bucket `administratorjhranacloud`
- File upload dir: `uploads/photos`
- JWT: 86400000ms (24 hours) expiration, secret from secrets file
- Admin credentials from secrets file

### `frontend/vite.config.js`
- Vite dev server on port 5173
- Proxy `/api`, `/login`, `/logout` to `http://localhost:8080`

### `config/settings.txt`
- Empty file

### `build-and-run.bat`
- Deletes `target/` directory
- Runs Maven package with `-DskipTests` using JDK 25 javac
- Runs the JAR directly with `java -jar`

---

## KEY ARCHITECTURAL NOTES

1. **Authentication flow:** JWT-based with `Bearer` token in `Authorization` header. Login endpoint is `/api/auth/login`. Token is stored in localStorage and added to requests via Axios interceptor.

2. **Security is intentionally loose:** The `SecurityConfig` assigns `ROLE_ADMIN` to ALL users. The `JwtAuthenticationFilter` whitelists most `/api/reports/**` paths, effectively making reports API public. This appears to be a development/demo setup.

3. **Three data ingestion paths:**
   - Manual upload via multipart/form-data (web UI)
   - Mobile app submission via JSON (`/api/reports` POST with `ReportSubmissionDTO`)
   - Email polling via IMAP (every 30 minutes, processes attachments as reports)

4. **S3 integration:** The `ReportScheduler` polls S3 every 5 minutes for new report folders containing `report.json`, processes them, and moves them to `processed/`.

5. **File types supported:** HTML, PDF, TXT, DOC, DOCX (local storage). Photos are downloaded from S3 and saved to local `uploads/photos/` directory.

6. **Database model hierarchy:** Report -> (has many) Rounds -> (has many) CheckpointLogs and Violations -> (has many) Incidents

---

## ALL FILES IN PROJECT

**Backend Java files (43 files):**
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/AdministratorJhranaApplication.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/CustomUserDetails.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/DataInitializer.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/JwtAuthenticationFilter.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/S3Config.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/SecurityConfig.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/config/WebConfig.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/controller/AuthController.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/controller/ReportController.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/controller/SearchController.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/controller/SearchService.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/controller/StatisticsController.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/IncidentDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/LogDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/LogEntryDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/LoginRequest.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/ReportDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/ReportSubmissionDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/RoundDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/dto/ViolationDTO.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/exception/GlobalExceptionHandler.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/CheckpointLog.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/Incident.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/Report.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/Round.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/User.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/model/Violation.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/repository/CheckpointLogRepository.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/repository/IncidentRepository.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/repository/ReportRepository.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/repository/RoundRepository.java`
- `C:/Users/Николай/IdeaProjects/Administrator Jhrana/src/main/java/com/administratorjhrana/repository


Отлично, я получил полное представление о проекте. Вот README:

---

# Administrator Jhrana

Веб-приложение для управления отчётами об обходах охраны, мониторинга нарушений и анализа инцидентов. Система собирает, хранит и обрабатывает отчёты нарядов охраны, генерирует статистику и поддерживает хранение файлов (локальное и Yandex Cloud S3).

## 📋 Содержание

- [Технологии](#-технологии)
- [Архитектура](#-архитектура)
- [Возможности](#-возможности)
- [Структура проекта](#-структура-проекта)
- [Установка и запуск](#-установка-и-запуск)
- [API](#-api)
- [Модель данных](#-модель-данных)
- [Конфигурация](#-конфигурация)

---

## 🛠 Технологии

### Backend
| Технология | Версия | Назначение |
|---|---|---|
| Java | 17 | Язык разработки |
| Spring Boot | 3.2.0 | Основной фреймворк |
| Spring Security | bundled | Аутентификация и авторизация |
| Spring Data JPA | bundled | ORM (Hibernate) |
| PostgreSQL | 14+ | Основная база данных |
| H2 | bundled | Консоль администрирования БД |
| JWT (jjwt) | 0.12.5 | Токены аутентификации |
| AWS SDK S3 | 2.25.60 | Хранение файлов (Yandex Cloud) |
| Lombok | 1.18.32 | Генерация шаблонного кода |

### Frontend
| Технология | Версия | Назначение |
|---|---|---|
| Vue.js | 3.4+ | UI-фреймворк |
| Vue Router | 4.2.5 | Маршрутизация |
| Pinia | 2.1.7 | Управление состоянием |
| Axios | 1.6+ | HTTP-клиент |
| Chart.js | 4.4+ | Визуализация данных |
| XLSX | 0.18.5 | Экспорт в Excel |
| Vite | 5.0+ | Сборщик |

---

## 🏗 Архитектура

Проект построен по модели клиент-сервер с разделением на backend (Spring Boot) и frontend (Vue.js):

```
┌─────────────┐      ┌──────────────────┐      ┌───────────┐
│   Frontend  │─────▶│   Spring Boot    │─────▶│ PostgreSQL│
│  Vue 3 +    │  WS  │   Backend API    │      │  Database │
│  Pinia      │      │                  │      └───────────┘
└─────────────┘      └────────┬─────────┘
                              │
                              ▼
                       ┌───────────┐
                       │Yandex S3  │
                       │  Storage  │
                       └───────────┘
```

### Пути загрузки данных
1. **Ручная загрузка** через веб-интерфейс (multipart/form-data)
2. **Мобильное приложение** — отправка JSON с данными обхода (`ReportSubmissionDTO`)
3. **Email-почта** — автоматический парсинг IMAP (каждые 30 минут), сохранение вложений как отчётов
4. **S3 Scheduler** — автоматическая обработка папок `reports/` в S3 (каждые 5 минут)

---

## ✨ Возможности

- 🔐 JWT-аутентификация (login/refresh/verify)
- 📋 CRUD-операции с отчётами обходов
- 🔍 Поиск и фильтрация (по охране, названию, дате)
- 📊 Статистика нарушений и времени прохождения контрольных точек
- 📈 Графики (Chart.js) с экспортом в Excel/CSV
- 📎 Поддержка файлов: HTML, PDF, TXT, DOC, DOCX
- 📧 Отправка отчётов по email и проверка входящих
- ☁️ Интеграция с Yandex Cloud S3
- 🔄 Автоматическая обработка данных из S3 и email
- 📱 Адаптивный интерфейс для мобильных устройств

---

## 📂 Структура проекта

```
Administrator Jhrana/
├── src/main/java/com/administratorjhrana/
│   ├── config/              # Security, JWT, S3, CORS, инициализация данных
│   ├── controller/          # REST API endpoints
│   ├── dto/                 # Data Transfer Objects
│   ├── exception/           # Глобальная обработка ошибок
│   ├── model/               # JPA-сущности
│   ├── repository/          # Data Access Layer
│   ├── scheduler/           # Планировщики (S3 polling)
│   └── service/             # Бизнес-логика
├── src/main/resources/
│   └── application.yml      # Конфигурация приложения
├── frontend/src/
│   ├── api/                 # HTTP-клиент (Axios)
│   ├── components/          # Переиспользуемые компоненты
│   ├── router/              # Маршруты Vue Router
│   ├── stores/              # Pinia stores (auth)
│   └── views/               # Страницы (Login, Reports, Statistics)
├── build-and-run.bat        # Скрипт сборки и запуска
└── pom.xml                  # Maven-конфигурация
```

---

## 🚀 Установка и запуск

### Требования
- JDK 17+
- Maven 3.6+
- Node.js 18+
- PostgreSQL 14+
- (Опционально) Yandex Cloud S3 bucket

### 1. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE jhrana_db;
```

Убедитесь, что PostgreSQL доступен на `localhost:5440`.

### 2. Настройка конфигурации

Создайте файл `config/application-secrets.yml` рядом с проектом:

```yaml
app:
  admin:
    username: admin
    password: admin123
  jwt-secret: your-secret-key-here
mail:
  username: your-email@gmail.com
  password: your-app-password
s3:
  access-key: your-yandex-access-key
  secret-key: your-yandex-secret-key
```

### 3. За backend

```bash
# Сборка и запуск (Windows)
build-and-run.bat

# Или вручную
mvn clean package -DskipTests
java -jar target/administrator-jhrana-0.0.1-SNAPSHOT.jar
```

Backend запустится на `http://localhost:8080`.

### 4. За frontend (отдельный терминал)

```bash
cd frontend
npm install
npm run dev
```

Frontend запустится на `http://localhost:5173` с проксированием API на `localhost:8080`.

---

## 🔌 API

### Аутентификация

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/api/auth/login` | Вход, получение JWT |
| POST | `/api/auth/refresh` | Обновление токена |
| POST | `/api/auth/verify` | Проверка токена |

**Request (login):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin"
}
```

### Отчёты

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/api/reports` | Создать отчёт (JSON/mobile) |
| POST | `/api/reports/upload` | Загрузить файл |
| GET | `/api/reports` | Список отчётов (пагинация) |
| GET | `/api/reports/{id}` | Получить отчёт по ID |
| PUT | `/api/reports/{id}` | Обновить отчёт |
| DELETE | `/api/reports/{id}` | Удалить отчёт |
| GET | `/api/reports/search?query=` | Поиск отчётов |
| POST | `/api/reports/{id}/email` | Отправить отчёт по email |
| POST | `/api/reports/check-emails` | Проверить входящие email |

### Статистика

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/statistics/violations` | Нарушения по типам |
| GET | `/api/statistics/checkpoints` | Время прохождения КТ |

### Консоль H2 (для отладки БД)

`http://localhost:8080/h2-console`

---

## 🗄 Модель данных

Иерархия сущностей:

```
Report (Отчёт)
├── Round (Обход)
│   ├── CheckpointLog (Журнал КТ)
│   └── Violation (Нарушение)
│       └── Incident (Инцидент)
```

### Сущности

**User** — пользователь системы (username, password BCrypt)

**Report** — главный отчёт об обходе:
- `title` — заголовок
- `guardName` — имя охранника
- `date` — дата
- `endTime` — время окончания
- `htmlContent`, `pdfUrl`, `htmlUrl`, `filePath` — файлы отчёта
- `notes` — примечания

**Round** — конкретный обход в рамках отчёта:
- `roundNumber`, `location`
- `startTime`, `endTime`

**Violation** — тип нарушения:
- `type`, `description`, `severity`
- `imageUrls`, `detectedAt`

**Incident** — инцидент (присваивается сотруднику, статус, заметки)

**CheckpointLog** — журнал прохождения контрольной точки:
- `checkpointName`, `timestamp`, `routeName`
- `sequenceIndex`, `isSequenceCorrect`, `scanType`

---

## ⚙️ Конфигурация

### application.yml

Основные параметры в `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5440/jhrana_db
  jpa:
    hibernate:
      ddl-auto: update
  mail:
    host: smtp.gmail.com
    port: 587

app:
  jwt-expiration: 86400000  # 24 часа
```

Секреты загружаются из `./config/application-secrets.yml` (импортируется через `spring.config.import`).

### Vite (frontend)

```javascript
// vite.config.js
export default defineConfig({
  server: { port: 5173 },
  proxy: {
    '/api': 'http://localhost:8080',
    '/login': 'http://localhost:8080',
    '/logout': 'http://localhost:8080'
  }
})
```

---

## 👥 Роли и безопасность

На текущей версии (`ROLE_ADMIN`) все пользователи получают роль администратора. Фильтр `JwtAuthenticationFilter` открывает публичный доступ к `/api/reports/**` и `/reports/**` для удобства интеграции с мобильным приложением.

**⚠️ Это конфигурация для демонстрации/разработки. Для production требуется доработка авторизации.**

---

## 📧 Email-интеграция

Система автоматически проверяет email (`imap.yandex.ru`) каждые 30 минут:
- Сканирует входящие сообщения от определённых отправителей
- Сохраняет вложения как отчёты
- Помечает обработанные письма

Настройки IMAP в `application-secrets.yml`.

---

## ☁️ Yandex Cloud S3

- **Endpoint:** `storage.yandexcloud.net`
- **Region:** `ru-central1`
- **Bucket:** `administratorjhranacloud`

Планировщик (`ReportScheduler`) каждые 5 минут сканирует папку `reports/` на наличие `report.json`, обрабатывает отчёты и перемещает папки в `processed/`.

---

## 📝 Разработка

### Рекомендуемый IDE
IntelliJ IDEA (auto-detect Maven), VS Code (для frontend)

### Lombok
Убедитесь, что Lombok-плагин включён в IDE.

### Структура команды (одиночная разработка)
Монорепозиторий с backend и frontend в одном проекте.

---

## ⚠️ Известные проблемы

1. **Violation setters** — сеттеры в сущности `Violation` переопределены как no-op (вероятно, баг генерации Lombok)
2. **Уровни доступа** — все пользователи имеют `ROLE_ADMIN`

---

## 📄 Лицензия

Внутренний проект.

---

**Автор:** Administrator Jhrana Team  
**Версия:** 0.0.1-SNAPSHOT  
**Spring Boot:** 3.2.0 | **Vue.js:** 3.4
