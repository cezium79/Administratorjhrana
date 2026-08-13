🛡️ Jhrana — Система контроля обходов охранников
Описание проекта

Jhrana — это корпоративная система сбора и анализа данных об обходах территории. Она принимает детализированные данные с Android-устройств (точки маршрута, фото, выявленные нарушения), хранит их в реляционной базе данных и предоставляет веб-интерфейс для управления инцидентами.

Ключевая особенность архитектуры: В отличие от систем, хранящих только ссылки на файлы, Jhrana сохраняет полный HTML-код отчета непосредственно в базу данных (поле htmlContent типа TEXT), обеспечивая сохранность истории даже при удалении исходных медиа-файлов с диска.

Технологический стек
Слой	Технологии
Backend	Java 17, Spring Boot 3.2.0, Spring Data JPA, Spring Security (JWT), Maven
Frontend	Vue.js 3, Pinia, Axios
Database	H2 Database (dev), PostgreSQL / MySQL (prod)
Маппинг	Lombok (для сокращения бойлерплейта)
Структура базы данных

Система использует нормализованную структуру из шести основных таблиц:

reports (Отчеты): Главная сущность. Хранит метаданные смены (guard_name, date) и полное тело документа в формате HTML.
rounds (Обходы): Дочерняя таблица отчетов. Один отчет может содержать несколько выходов. Содержит время начала/конца (start_time, end_time) и локацию.
checkpoint_logs (Точки маршрута): Детализация каждого обхода. Каждая сканированная NFC/QR-метка фиксируется как отдельная запись с порядковым номером (sequence_index) и флагом правильности последовательности (is_sequence_correct).
violations (Нарушения): Фиксация проблем во время чекина. Привязана к конкретному round_id. Содержит тип проблемы, описание и URL изображений.
incidents (Инциденты): Задачи на исправление. Связаны с нарушениями (violation_id). Имеют статус (status) и назначенного исполнителя (assigned_to).
users (Пользователи): Таблица авторизации. Хранит логины и пароли для доступа к системе.
Иерархия связей
text
Копировать
[User] 1 <---> * [Report] 1 <---> * [Round] 1 <---> * [CheckpointLog]
                                      |
                                      +---> * [Violation] 1 <---> * [Incident]
API Endpoints (Примеры)

Все эндпоинты требуют JWT-токен в заголовке Authorization: Bearer <token>.

Управление отчетами

Основано на репозитории ReportRepository.

Получение списка (с пагинацией и поиском):

http
Копировать
GET /api/reports?page=0&size=20&sortBy=uploadedAt&direction=DESC&guardName=Иван

Логика: Вызывает findByGuardNameContainingIgnoreCase().

Фильтр по дате:

http
Копировать
GET /api/reports?dateFrom=2026-08-01T00:00:00&dateTo=2026-08-13T23:59:59

Логика: Вызывает кастомный @Query findByDateBetween().

Получение справочников для фильтров:

http
Копировать
GET /api/reports/filters/guards

Логика: Вызывает findDistinctGuardNames() для формирования выпадающего списка.

Работа с данными обходов
Создание точки (Check-in):
POST /api/checkpoint-logs
Принимает JSON с координатами, названием точки и статусом последовательности.
Выявление нарушения:
POST /api/violations
Создает запись в таблице violations, привязанную к текущему round_id.
Конфигурация БД

По умолчанию используется встроенная база H2. Для перехода на production-системы достаточно изменить блок spring.datasource в application.yml.

Пример для PostgreSQL:

yaml
Копировать
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jhrana
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update # Автоматическое создание таблиц по @Entity классам
    database-platform: org.hibernate.dialect.PostgreSQLDialect
Особенности реализации безопасности
Аутентификация: Реализована через фильтры Spring Security и JWT-токены.
Хранение паролей: В текущей схеме таблицы users поле password имеет тип String. Рекомендуется интегрировать PasswordEncoder (BCrypt) для хеширования паролей перед записью в БД.
Типизация времени: В сущностях Round и Violation поля времени хранятся как String. Для корректной сортировки и фильтрации рекомендуется мигрировать их на стандартный тип java.time.LocalDateTime.
Быстрый запуск
bash
Копировать
# 1. Сборка Backend (автоматически подтянет зависимости из pom.xml)
mvn clean package
java -jar target/jhrana-0.0.1-SNAPSHOT.jar

# 2. Запуск Frontend
cd frontend && npm install && npm run dev

Учетная запись администратора создается автоматически при первом старте приложения средствами инициализации данных.
</>
Выполнил код
python
Копировать
import graphviz
from io import StringIO

# Определение структуры сущностей и связей на основе предоставленного Java-кода
dot = graphviz.Digraph(comment='ER-диаграмма Jhrana', format='png')
dot.attr(rankdir='LR', size='12,8', dpi='300', fontname='Arial')

# Настройка узлов (таблиц)
node_attrs = {
    'shape': 'plaintext',
    'fontname': 'Arial',
    'fontsize': '10'
}

# Определение сущностей как HTML-таблиц для красивого отображения полей
entities = {
    'User': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightblue"><B>users</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="username">username (UQ)</TD></TR>
            <TR><TD PORT="password">password</TD></TR>
        </TABLE>
    ''',
    'Report': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightgreen"><B>reports</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="title">title</TD></TR>
            <TR><TD PORT="guardName">guard_name</TD></TR>
            <TR><TD PORT="date">date</TD></TR>
            <TR><TD PORT="htmlContent">htmlContent (TEXT)</TD></TR>
            <TR><TD PORT="pdfUrl">pdf_url</TD></TR>
            <TR><TD PORT="filePath">file_path</TD></TR>
            <TR><TD PORT="uploadedAt">uploaded_at</TD></TR>
            <TR><TD PORT="notes">notes</TD></TR>
        </TABLE>
    ''',
    'Round': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightyellow"><B>rounds</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="report_id">report_id (FK)</TD></TR>
            <TR><TD PORT="roundNumber">round_number</TD></TR>
            <TR><TD PORT="location">location</TD></TR>
            <TR><TD PORT="startTime">start_time</TD></TR>
            <TR><TD PORT="endTime">end_time</TD></TR>
            <TR><TD PORT="filePath">file_path</TD></TR>
        </TABLE>
    ''',
    'CheckpointLog': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightgrey"><B>checkpoint_logs</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="round_id">round_id (FK)</TD></TR>
            <TR><TD PORT="checkpointId">checkpoint_id</TD></TR>
            <TR><TD PORT="timestamp">timestamp</TD></TR>
            <TR><TD PORT="sequenceIndex">sequence_index</TD></TR>
            <TR><TD PORT="isSequenceCorrect">is_sequence_correct</TD></TR>
            <TR><TD PORT="photoPath">photo_path</TD></TR>
        </TABLE>
    ''',
    'Violation': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightsalmon"><B>violations</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="round_id">round_id (FK)</TD></TR>
            <TR><TD PORT="type">type</TD></TR>
            <TR><TD PORT="severity">severity</TD></TR>
            <TR><TD PORT="imageUrls">image_urls</TD></TR>
            <TR><TD PORT="detectedAt">detected_at</TD></TR>
        </TABLE>
    ''',
    'Incident': '''
        <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
            <TR><TD BGCOLOR="lightpink"><B>incidents</B></TD></TR>
            <TR><TD PORT="id">id (PK)</TD></TR>
            <TR><TD PORT="violation_id">violation_id (FK)</TD></TR>
            <TR><TD PORT="status">status</TD></TR>
            <TR><TD PORT="assignedTo">assigned_to</TD></TR>
            <TR><TD PORT="resolvedAt">resolved_at</TD></TR>
        </TABLE>
    '''
}

# Добавление узлов
for name, label in entities.items():
    dot.node(name, f'<<{label}>>', **node_attrs)

# Определение связей (отношений)
# User (1) -> (*) Report (неявная связь через security, но в БД напрямую не выражена в этих сущностях, пропустим для чистоты схемы)

# Report (1) -> (*) Round
dot.edge('Report:id', 'Round:report_id', 
         dir='both', arrowhead='crow', arrowtail='odot', 
         label='1..*', fontsize='9', fontname='Arial')

# Round (1) -> (*) CheckpointLog
dot.edge('Round:id', 'CheckpointLog:round_id', 
         dir='both', arrowhead='crow', arrowtail='odot', 
         label='1..*', fontsize='9', fontname='Arial')

# Round (1) -> (*) Violation
dot.edge('Round:id', 'Violation:round_id', 
         dir='both', arrowhead='crow', arrowtail='odot', 
         label='1..*', fontsize='9', fontname='Arial')

# Violation (1) -> (*) Incident
dot.edge('Violation:id', 'Incident:violation_id', 
         dir='both', arrowhead='crow', arrowtail='odot', 
         label='1..*', fontsize='9', fontname='Arial')

# Подписи к кластерам для наглядности
with dot.subgraph(name='cluster_0') as c:
    c.attr(label='Справочники и Безопасность', fontname='Arial Bold', fontsize='12')
    c.node('User')

with dot.subgraph(name='cluster_1') as c:
    c.attr(label='Основная логика обходов', fontname='Arial Bold', fontsize='12')
    c.node('Report')
    c.node('Round')
    c.node('CheckpointLog')

with dot.subgraph(name='cluster_2') as c:
    c.attr(label='Управление инцидентами', fontname='Arial Bold', fontsize='12')
    c.node('Violation')
    c.node('Incident')

# Рендеринг
img_data = dot.pipe(format='png')
with open('/tmp/er_diagram.png', 'wb') as f:
    f.write(img_data)

Схема базы данных проекта Jhrana

В системе используется шесть основных таблиц. Ниже приведена их структура и типы связей:

1. users

id (PK, Long)
username (String, Unique, Not Null)
password (String, Not Null)
Назначение: Аутентификация администраторов.

2. reports (Корневая сущность)

id (PK, Long)
title (String)
guard_name (String)
date (LocalDateTime)
htmlContent (TEXT/Lob) — полное тело отчета
pdf_url (String)
file_path (String)
uploaded_at (LocalDateTime)
notes (String)
Связи: 1 Report содержит много Rounds.

3. rounds (Детализация обхода)

id (PK, Long)
report_id (FK → reports.id)
round_number (Integer)
location (String)
start_time / end_time (String)
file_path (String)
Связи: 1 Round принадлежит 1 Report; 1 Round содержит много CheckpointLogs и Violations.

4. checkpoint_logs (Точки маршрута)

id (PK, Long)
round_id (FK → rounds.id)
checkpoint_id / checkpoint_name (String)
timestamp (LocalDateTime)
sequence_index (Integer)
is_sequence_correct (Boolean)
photo_path (String)
Связи: Принадлежит одному конкретному заходу (Round).

5. violations (Выявленные проблемы)

id (PK, Long)
round_id (FK → rounds.id)
type / description / severity (String)
image_urls (String)
detected_at (String)
Связи: Фиксируется во время конкретного Round. 1 Violation может породить много Incidents.

6. incidents (Задачи на исправление)

id (PK, Long)
violation_id (FK → violations.id)
status (String)
assigned_to (String)
resolved_at (String)
Связи: Является следствием выявленного нарушения (Violation).
Визуальное представление иерархии
text
Копировать
[USERS]
   |
   v (авторизует доступ к...)
[REPORTS] <------------------------------------+
   |                                           |
   +---< [ROUNDS] ---< [CHECKPOINT_LOGS]       |
   |        |                                  |
   |        +---< [VIOLATIONS] ---< [INCIDENTS]
   |
   +--- Хранит HTML-код самого документа

Направления отношений (Cardinality):

reports 1 — * rounds: Один отчет объединяет несколько выходов охранника.
rounds 1 — * checkpoint_logs: Каждый выход состоит из множества сканированных точек.
rounds 1 — * violations: За один выход можно найти несколько проблем.
violations 1 — * incidents: На одну поломку могут назначить нескольких исполнителей или создать цепочку задач.

