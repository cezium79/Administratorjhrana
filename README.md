🛡️ Jhrana — Система контроля обходов охранников

Jhrana — полнофункциональная система для приёма, хранения и управления отчётами обхода от Android-приложений и веб-интерфейса.

✨ Возможности
📱 Отправка отчётов из Android-приложения (файлы или URL)
🔐 Безопасная авторизация через JWT-токены
📊 Панель администратора для просмотра и управления отчётами
🔍 Продвинутая фильтрация и поиск
📧 Отправка отчётов на email
📄 Поддержка HTML и PDF форматов
🗃️ Хранение данных в H2 (с возможностью миграции на другие БД)
🛠️ Технологии
Backend
Java 17
Spring Boot 3.2.0
Spring Security (JWT аутентификация)
Spring Data JPA
Spring Mail
H2 Database (или другая RDBMS)
Maven
Frontend
Vue.js 3 (Composition API)
Vite
Vue Router 4
Pinia (состояние)
Axios (HTTP-клиент)
📋 Требования
Java 17 или выше
Maven 3.8+
Node.js 18+
npm или yarn
🚀 Быстрый старт
1. Клонирование репозитория
bash
Копировать
git clone <repository-url>
cd Administrator-Jhrana
2. Запуск Backend
bash
Копировать
# Установка зависимостей и запуск
mvn spring-boot:run

Или сборка и запуск:

bash
Копировать
mvn clean package
java -jar target/jhrana-0.0.1-SNAPSHOT.jar

Backend запустится на: http://localhost:8080

3. Запуск Frontend
bash
Копировать
cd frontend
npm install
npm run dev

Frontend запустится на: http://localhost:5173

4. Вход в систему

Первоначальные учётные данные:

Логин: admin
Пароль: admin123
📡 API Endpoints
Авторизация
Метод	Endpoint	Описание
POST	/login	Форма логина (веб)
Управление отчётами (требует авторизации)
Метод	Endpoint	Описание
POST	/api/reports	Создание отчёта (Multipart или URL)
GET	/api/reports	Список отчётов
GET	/api/reports/{id}	Детали отчёта
PUT	/api/reports/{id}	Редактирование метаданных
DELETE	/api/reports/{id}	Удаление отчёта
GET	/api/reports/{id}/html	Просмотр HTML
GET	/api/reports/{id}/pdf	Просмотр PDF
GET	/api/reports/{id}/download	Скачивание файла
POST	/api/reports/{id}/email	Отправка на email
GET	/api/reports/filters	Фильтры (охранники, названия)
Параметры фильтрации
Параметр	Тип	Описание	По умолчанию
page	integer	Номер страницы	0
size	integer	Размер страницы	20
sortBy	string	Поле сортировки	uploadedAt
direction	string	Направление (ASC/DESC)	DESC
guardName	string	Фильтр по имени охранника	-
title	string	Поиск по названию	-
dateFrom	string	Дата с (ISO 8601)	-
dateTo	string	Дата по (ISO 8601)	-
📤 Отправка отчёта из Android
Вариант 1: Multipart (загрузка файла)
http
Копировать
POST /api/reports
Content-Type: multipart/form-data

file: [binary file]
guardName: "Иванов И.И."
title: "Обход территории 11.08.2026"
date: "2026-08-11T14:30:00"
notes: "Все в порядке"
Вариант 2: JSON (по URL)
http
Копировать
POST /api/reports
Content-Type: application/json

{
  "url": "https://example.com/report.html",
  "guardName": "Иванов И.И.",
  "title": "Обход территории 11.08.2026",
  "date": "2026-08-11T14:30:00",
  "notes": "Все в порядке"
}
⚙️ Настройка
Конфигурация приложения

Основной конфигурационный файл: src/main/resources/application.yml

Настройка SMTP для отправки email
yaml
Копировать
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
Настройка базы данных

По умолчанию используется H2 Database (встроенная). Для production рекомендуется PostgreSQL или MySQL.

yaml
Копировать
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jhrana
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
📁 Структура проекта
Копировать
Administrator Jhrana/
├── src/main/java/com/administratorjhrana/
│   ├── AdministratorJhranaApplication.java  # Точка входа
│   ├── config/                              # Конфигурации (Security, CORS, инициализация данных)
│   ├── model/                              # JPA-модели (Report, User)
│   ├── repository/                         # Репозитории (JPA)
│   ├── service/                            # Бизнес-логика
│   ├── controller/                         # REST API контроллеры
│   ├── dto/                                # DTO объекты
│   └── exception/                          # Обработка исключений
├── src/main/resources/
│   ├── application.yml                      # Конфигурация приложения
│   └── static/                             # Статические файлы (frontend)
├── frontend/                                # Vue.js фронтенд
│   ├── src/
│   │   ├── api/                           # HTTP-клиенты
│   │   ├── assets/                        # Статика (CSS, изображения)
│   │   ├── components/                    # Vue-компоненты
│   │   ├── router/                        # Vue Router
│   │   ├── stores/                        # Pinia stores
│   │   └── views/                         # Страницы
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── pom.xml                                  # Maven конфигурация
└── README.md
🏗️ Production Build
Фронтенд
bash
Копировать
cd frontend
npm run build
Backend (с включённым фронтендом)
bash
Копировать
mvn clean package
java -jar target/jhrana-0.0.1-SNAPSHOT.jar
Docker (опционально)
bash
Копировать
docker build -t jhrana:latest .
docker run -p 8080:8080 jhrana:latest
🔑 Безопасность
JWT токены для аутентификации
Spring Security с ролевой моделью
Валидация входных данных
Защита от SQL-инъекций через JPA
CORS конфигурация
🧪 Тестирование
bash
Копировать
# Запуск всех тестов
mvn test

# С покрытием
mvn test jacoco:report
📝 Лицензия

MIT License

👥 Авторы
Разработано как часть проекта Administrator Jhrana
📞 Поддержка

При возникновении проблем, создайте issue в репозитории.

⭐ Если проект полезен, поставьте звёздочку!

</details>
