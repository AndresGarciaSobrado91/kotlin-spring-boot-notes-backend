# Spring Boot with Kotlin - Backend

A Spring Boot backend application built with Kotlin for managing notes.

## Technologies

- Spring Boot
- Kotlin
- Gradle
- JPA/Hibernate

## Features

- User management
- Notes CRUD operations
- Authentication with refresh tokens

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle (included via wrapper)

### Running the Application

```bash
./gradlew bootRun
```

### Building the Application

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

## API Endpoints

### Authentication (`/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Login and receive tokens | No |
| POST | `/auth/refresh` | Refresh access token | No |

**Register/Login Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Refresh Request Body:**
```json
{
  "refreshToken": "your-refresh-token"
}
```

**Token Response (login/refresh):**
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token"
}
```

### Notes (`/notes`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/notes` | Create a new note | Yes |
| GET | `/notes` | Get all notes for authenticated user | Yes |
| DELETE | `/notes/{id}` | Delete a note by ID | Yes |

**Note Request Body (POST):**
```json
{
  "title": "My Note",
  "content": "Note content here",
  "color": 16777215
}
```

**Note Response:**
```json
{
  "id": "note-id",
  "title": "My Note",
  "content": "Note content here",
  "color": 16777215,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Authentication Header:**
```
Authorization: Bearer <access-token>
```

## Project Structure

- `src/main/kotlin` - Application source code
- `src/test/kotlin` - Test source code
- `build.gradle.kts` - Gradle build configuration

## License

This project is for educational purposes.
