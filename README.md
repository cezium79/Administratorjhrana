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
