# Система контроля обходов охранников

Веб-приложение для приёма, хранения и управления отчётами обхода от Android-приложения.

## Требования

- Java 17+
- Maven 3.8+
- Node.js 18+

## Запуск

### 1. Backend (Spring Boot)

```powershell
# Запуск в режиме разработки
mvn spring-boot:run

# Или сборка и запуск
mvn clean package
java -jar target/jhrana-0.0.1-SNAPSHOT.jar
```

Backend запускается на `http://localhost:8080`

**Логин по умолчанию:**
- Пользователь: `admin`
- Пароль: `admin123`

### 2. Frontend (Vue.js)

```powershell
cd frontend
npm install
npm run dev
```

Frontend запускается на `http://localhost:5173`

## Построение для production

```powershell
# Frontend
cd frontend
npm run build

# Backend (копирует frontend в static)
cd ..
mvn clean package
java -jar target/jhrana-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Авторизация
- `POST /login` — форма логина (для веб-интерфейса)

### Отчёты (требуют авторизации)
- `POST /api/reports` — отправка отчёта из URL (JSON: `{"url": "..."}`)
- `POST /api/reports` — загрузка файла multipart/form-data
- `GET /api/reports` — список отчётов (paginated)
- `GET /api/reports/{id}` — детали отчёта
- `PUT /api/reports/{id}` — редактирование метаданных
- `DELETE /api/reports/{id}` — удаление
- `GET /api/reports/{id}/html` — просмотр HTML
- `GET /api/reports/{id}/pdf` — просмотр PDF
- `GET /api/reports/{id}/download` — скачивание файла
- `POST /api/reports/{id}/email` — отправка на email
- `GET /api/reports/filters` — фильтры (охранники, названия)

### Параметры фильтрации (GET /api/reports)
- `page` — номер страницы (default: 0)
- `size` — размер страницы (default: 20)
- `sortBy` — поле сортировки (default: uploadedAt)
- `direction` — направление (ASC/DESC, default: DESC)
- `guardName` — фильтр по имени охранника
- `title` — поиск по названию
- `dateFrom` — дата с (ISO format)
- `dateTo` — дата по (ISO format)

### Отправка отчёта из Android

**Вариант 1: Multipart**
```http
POST /api/reports
Content-Type: multipart/form-data

file: [binary file]
guardName: "Иванов И.И."
title: "Обход территории 11.08.2026"
date: "2026-08-11T14:30:00"
notes: "Все в порядке"
```

**Вариант 2: URL**
```json
POST /api/reports
Content-Type: application/json

{
  "url": "https://example.com/report.html",
  "guardName": "Иванов И.И.",
  "title": "Обход территории 11.08.2026",
  "date": "2026-08-11T14:30:00",
  "notes": "Все в порядке"
}
```

## Конфигурация

Файл `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

Для отправки email нужно настроить SMTP-сервер в application.yml.

## Структура проекта

```
├── src/main/java/com/administratorjhrana/
│   ├── AdministratorJhranaApplication.java
│   ├── config/          — Security, CORS, DataInitializer
│   ├── model/           — Report, User
│   ├── repository/      — JPA repositories
│   ├── service/         — Business logic
│   ├── controller/      — REST endpoints
│   ├── dto/             — Data Transfer Objects
│   └── exception/       — Exception handlers
├── src/main/resources/
│   └── application.yml
├── frontend/
│   ├── src/
│   │   ├── api/         — HTTP client
│   │   ├── assets/      — CSS
│   │   ├── components/  — Vue components
│   │   ├── router/      — Vue Router
│   │   ├── stores/      — Pinia stores
│   │   └── views/       — Page views
│   ├── package.json
│   └── vite.config.js
├── pom.xml
└── README.md
```
