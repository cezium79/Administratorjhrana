# Administrator Jhrana

**Administrator Jhrana** — веб-приложение для управления отчётностью, мониторинга нарушений и анализа инцидентов. Система обеспечивает сбор, хранение и обработку отчётов, генерацию статистики, а также работу с хранилищем файлов.

## 📋 Содержание

- [Стек технологий](#-стек-технологий)
- [Архитектура](#-архитектура)
- [Возможности](#-возможности)
- [Требования](#-требования)
- [Установка и запуск](#-установка-и-запуск)
- [Структура проекта](#-структура-проекта)
- [API](#-api)
- [Конфигурация](#-конфигурация)
- [Тестирование](#-тестирование)

---

## 🛠 Стек технологий

### Backend
| Технология | Версия | Назначение |
|---|---|---|
| Java | 17 | Язык программирования |
| Spring Boot | 3.2.0 | Основной фреймворк |
| Spring Security | часть Spring Boot | Аутентификация и авторизация |
| Spring Data JPA | часть Spring Boot | ORM и работа с БД |
| PostgreSQL | — | Основная СУБД |
| H2 | — | Консоль для администрирования БД |
| JWT (jjwt) | 0.12.5 | Токены аутентификации |
| AWS SDK S3 | 2.25.60 | Работа с объектным хранилищем (Yandex Cloud) |
| Lombok | 1.18.32 | Генерация boilerplate-кода |

### Frontend
| Технология | Версия | Назначение |
|---|---|---|
| Vue.js | 3.4+ | Фреймворк UI |
| Vue Router | 4.2.5 | Маршрутизация |
| Pinia | 2.1.7 | Управление состоянием |
| Axios | 1.6+ | HTTP-клиент |
| Chart.js | 4.4+ | Визуализация данных |
| vue-chartjs | 5.2+ | Интеграция Chart.js с Vue |
| Vite | 5.0+ | Сборщик |

---

## 🏗 Архитектура

Проект использует классическую трёхуровневую архитектуру:

```
┌─────────────────────────────────────────────┐
│              Frontend (Vue 3)               │
│   Vue Router │ Pinia │ Chart.js │ Axios     │
└──────────────────┬──────────────────────────┘
                   │ REST API (JSON)
┌──────────────────▼──────────────────────────┐
│              Backend (Spring Boot)          │
│  Controllers → Services → Repositories      │
│  Spring Security (JWT) │ Scheduling        │
└──────────────────┬──────────────────────────┘
                   │ JPA/Hibernate
┌──────────────────▼──────────────────────────┐
│    PostgreSQL (jhrana_db:5440)              │
└─────────────────────────────────────────────┘
```

---

## ✨ Возможности

- 🔐 **Аутентификация** — JWT-токены с срок действия 24 часа (настраивается)
- 📄 **Управление отчётами** — создание, просмотр, фильтрация и обработка отчётов
- 🚨 **Учёт инцидентов** — регистрация и отслеживание инцидентов
- ⚠️ **Реестр нарушений** — фиксация нарушений с привязкой к раундам
- 📊 **Статистика и аналитика** — дашборды с графиками (Chart.js)
- 🔍 **Поиск** — полноценный поиск по логам и записям
- 📎 **Хранение файлов** — поддержка локального файлового хранилища и Yandex Cloud S3
- 📧 **Email-сервис** — отправка уведомлений и приём отчётов по IMAP
- ⏰ **Планировщик** — автоматическая генерация отчётов по расписанию
- 🌐 **Vue 3 SPA** — SPA-интерфейс с роутингом, состоянием и визуализацией

---

## 📦 Требования

| Компонент | Минимальная версия |
|---|---|
| JDK | 17 |
| Maven | 3.8+ |
| Node.js | 18+ |
| npm | 9+ |
| PostgreSQL | 14+ |

---

## 🚀 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd "Administrator Jhrana"
```

### 2. Настройка конфигурации

Создайте файл `config/application-secrets.yml` в корневой директории проекта:

```yaml
# Пример структуры secrets-файла
spring:
  datasource:
    username: your_db_user
    password: your_db_password
  mail:
    username: your_email@gmail.com
    password: your_app_password

s3:
  access-key: your_yc_access_key
  secret-key: your_yc_secret_key

app:
  jwt:
    secret: your_jwt_secret_key_here
  admin:
    username: admin
    password: admin_password
```

> ⚠️ **Важно:** Файл `application-secrets.yml` **не должен** попадать в систему контроля версий.

### 3. Запуск Backend

```bash
# Сборка
mvn clean package

# Запуск
mvn spring-boot:run
```

Или через скрипт:
```bash
build-and-run.bat
```

Backend запустится на **http://localhost:8080**

### 4. Запуск Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend запустится на **http://localhost:5173** (Vite по умолчанию)

### 5. Консоль БД (H2)

Доступна по адресу **http://localhost:8080/h2-console** (для администрирования)

---

## 📁 Структура проекта

```
Administrator Jhrana/
├── config/                          # Конфигурационные файлы (секреты)
├── frontend/                        # Vue.js SPA-фронтенд
│   ├── src/
│   │   ├── api/                     # API-клиенты (axios)
│   │   ├── assets/                  # Статические ресурсы
│   │   ├── components/              # Переиспользуемые компоненты
│   │   │   ├── ReportPreviewModal.vue
│   │   │   └── ReportTypeBadge.vue
│   │   ├── router/                  # Маршруты Vue Router
│   │   ├── stores/                  # Pinia stores
│   │   ├── views/                   # Страницы приложения
│   │   │   ├── LoginView.vue
│   │   │   ├── ReportsView.vue
│   │   │   └── StatisticsPage.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── src/main/
│   ├── java/com/administratorjhrana/
│   │   ├── AdministratorJhranaApplication.java  # Точка входа
│   │   ├── config/                    # Конфигурации Spring
│   │   │   ├── CustomUserDetails.java
│   │   │   ├── DataInitializer.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── S3Config.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/                # REST-контроллеры
│   │   │   ├── AuthController.java
│   │   │   ├── ReportController.java
│   │   │   ├── SearchController.java
│   │   │   ├── StatisticsController.java
│   │   │   └── SearchService.java
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── IncidentDTO.java
│   │   │   ├── LogDTO.java
│   │   │   ├── LogEntryDTO.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── ReportDTO.java
│   │   │   ├── ReportSubmissionDTO.java
│   │   │   ├── RoundDTO.java
│   │   │   └── ViolationDTO.java
│   │   ├── exception/                 # Обработка исключений
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── model/                     # JPA-модели
│   │   │   ├── CheckpointLog.java
│   │   │   ├── Incident.java
│   │   │   ├── Report.java
│   │   │   ├── Round.java
│   │   │   ├── User.java
│   │   │   └── Violation.java
│   │   ├── repository/                # JPA-репозитории
│   │   │   ├── CheckpointLogRepository.java
│   │   │   ├── IncidentRepository.java
│   │   │   ├── ReportRepository.java
│   │   │   ├── RoundRepository.java
│   │   │   ├── StatisticsRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── ViolationRepository.java
│   │   ├── scheduler/                 # Планировщик задач
│   │   │   └── ReportScheduler.java
│   │   └── service/                   # Сервисный слой
│   │       ├── EmailService.java
│   │       ├── FileStorageService.java
│   │       ├── ImapReportReceiver.java
│   │       ├── JwtService.java
│   │       ├── ReportProcessingService.java
│   │       ├── ReportService.java
│   │       ├── S3StorageService.java
│   │       └── StorageService.java
│   └── resources/
│       └── application.yml            # Основная конфигурация
├── build-and-run.bat                  # Скрипт сборки и запуска
├── pom.xml                            # Maven-зависимости
├── README.md
└── План модернизации.docx
```

---

## 🔌 API

Backend предоставляет REST API на `http://localhost:8080/api/`:

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/api/auth/login` | Вход (получение JWT) |
| GET | `/api/reports` | Список отчётов |
| POST | `/api/reports` | Создание отчёта |
| GET | `/api/reports/{id}` | Детали отчёта |
| GET | `/api/statistics` | Статистика и метрики |
| POST | `/api/search` | Поиск по записям |

> Фронтенд взаимодействует с API через Axios-клиенты в `frontend/src/api/`.

---

## ⚙️ Конфигурация

### application.yml

Основные параметры конфигурации находятся в `src/main/resources/application.yml`:

| Параметр | Значение по умолчанию | Описание |
|---|---|---|
| `server.port` | `8080` | Порт сервера |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5440/jhrana_db` | Строка подключения к БД |
| `spring.jpa.hibernate.ddl-auto` | `update` | Автогенерация схемы |
| `spring.servlet.multipart.max-file-size` | `10MB` | Макс. размер загрузки |
| `app.jwt.expiration` | `86400000` (24ч) | Срок жизни JWT |
| `s3.endpoint` | `https://storage.yandexcloud.net` | Endpoint Yandex Cloud |
| `s3.bucket-name` | `administratorjhranacloud` | Имя бакета |

Все секретные значения вынесены в `config/application-secrets.yml`.

---

## 🧪 Тестирование

```bash
mvn test
```

Тесты используют **Spring Boot Test** и **Spring Security Test**.

---

## 📝 Лицензия

Внутренний проект.

---

## 👥 Контакты

По вопросам обращения к разработчику проекта.
