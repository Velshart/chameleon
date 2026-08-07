# Chameleon
A simple REST API for image processing, written in Spring Boot.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
    - [Authentication](#authentication)
    - [Images](#images)
    - [Image Transformation](#image-transformation)
- [Testing](#testing)

## Features
- **JWT Authentication** - User registration and login via Bearer tokens.
- **Rate Limiting** - IP-based request limiting.
- **Image Management** - Upload, retrieve, list, and delete images
- **Image Transformation** - Ability to chain multiple sequential transformations:
- `RESIZE` - Resize image dimensions
- `CROP` - Crop image regions
- `ROTATE` - Rotate by a specified angle
- `FLIP` - Flip vertically
- `MIRROR` - Flip horizontally
- `GRAYSCALE` - Apply grayscale filter
- `SEPIA` - Apply sepia filter
- `WATERMARK` - Add a text watermark
- `COMPRESS` - Adjust output quality compression
- `FORMAT` - Convert output format (e.g., to PNG/JPEG)
- **HATEOAS** - Each API response contains hypermedia links to related actions (`self`, `delete`, `transform`, `images`)

## Tech stack

| Category | Technology                       |
|---|----------------------------------|
| Language / Platform | Java 21+, Spring Boot 3          |
| Security | Spring Security, JWT             |
| Data Access | Spring Data JPA, Hibernate       |
| Database | PostgreSQL                       |
| Rate limiting | Bucket4j                         |
| Image Processing | Thumbnailator, Java AWT          |
| Hypermedia | Spring HATEOAS                   |
| Validation | Jakarta Bean Validation          |
| Testing | JUnit 5, Mockito, Testcontainers |

## Getting started

### Prerequisites
- Java 21+
- PostgreSQL (locally or in a container)
- Gradle (included wrapper `./gradlew`)

### Steps

1. Clone the repository.
2. Create a PostgreSQL database and fill in your values in `application.properties` (check [Configuration](#configuration))
3. Run the application:
```bash
./gradlew bootRun
```
4. By default, the server starts at `http://localhost:8080`.

## Configuration
Create an env.properties file in `/resources` and fill in your values.
A sample env.properties file is shown below:

```properties
# Database Configuration
DATASOURCE_URL=jdbc:postgresql://localhost:5432/chameleon
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=<base64-encoded-256-bit-key>
JWT_EXPIRATION=3600000
```
> **NOTE:** jwt.secret must be a random key of at least 256 bits (e.g., generated via openssl rand -base64 32)

## API Documentation
All endpoints except /api/auth/** require the following header:
```
Authorization: Bearer <token>
```

### Authentication

#### `POST /api/auth/register`

Registers a new user.

**Request:**
```json
{
    "username": "johndoe",
    "password": "securePassword123"
}
```

**Response `201 Created`:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### `POST /api/auth/login`

Authenticates an existing user.

**Request:**
```json
{
    "username": "johndoe",
    "password": "securePassword123"
}
```

**Response `200 OK`:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Images

#### `POST /api/images`

Uploads a new image using multipart/form-data with a file field.

**Response `201 Created`:**
```json
{
    "id": 1,
    "originalFilename": "cat.jpg",
    "contentType": "image/jpeg",
    "size": 245678,
    "uploadedAt": "2026-07-29T10:15:30Z",
    "_links": {
        "self": { "href": "http://localhost:8080/api/images/1" },
        "images": { "href": "http://localhost:8080/api/images" },
        "delete": { "href": "http://localhost:8080/api/images/1" },
        "transform": { "href": "http://localhost:8080/api/images/1/transform" }
    }
}
```

#### `GET /api/images`

Returns a paginated list of images owned by the currently authenticated user.

**Query parameters:**

| Parameter | Default (if not provided) | Description |
|---|---------------------------|---|
| `page` | `0`                       | Page number (zero-based) |
| `size` | `5`                       | Number of items per page |
| `sort` | `-`                       | Sorting criteria, e.g. `sort=uploadedAt,desc` |#### `GET /api/images/{id}`

**Example:** `GET /api/images?page=0&size=2&sort=uploadedAt,desc`

**Response `200 OK`:**
```json
{
  "_embedded": {
    "images": [
      {
        "_links": {
          "self": {
            "href": "http://localhost:8080/api/images/706"
          },
          "images": {
            "href": "http://localhost:8080/api/images"
          },
          "transform": {
            "href": "http://localhost:8080/api/images/706/transform"
          },
          "delete": {
            "href": "http://localhost:8080/api/images/706"
          }
        },
        "id": 706,
        "originalFilename": "cat.png",
        "contentType": "image/png",
        "size": 397765,
        "uploadedAt": "2026-08-06T13:00:57.852757Z"
      }
    ]
  },
  "_links": {
    "first": {
      "href": "http://localhost:8080/api/images?page=0&size=1&sort=uploadedAt,desc"
    },
    "self": {
      "href": "http://localhost:8080/api/images?page=0&size=1&sort=uploadedAt,desc"
    },
    "next": {
      "href": "http://localhost:8080/api/images?page=1&size=1&sort=uploadedAt,desc"
    },
    "last": {
      "href": "http://localhost:8080/api/images?page=5&size=1&sort=uploadedAt,desc"
    }
  },
  "page": {
    "number": 0,
    "size": 1,
    "totalElements": 6,
    "totalPages": 6
  }
}
```

### `GET /api/images/{id}`
Returns raw binary data for the image with the specified ID (if owned by the logged-in user). The Content-Type header matches the image format.

#### `DELETE /api/images/{id}`

Deletes the image with the specified `id`. Returns `204 No Content`.

### Image Transformation

#### `POST /api/images/{id}/transform`

Applies a sequence of transformations to an image and overwrites its data (and metadata) in the database. Returns the transformed raw image binary.

**Request:**
```json
{
    "transformations": [
        { "type": "RESIZE", "width": 800, "height": 600 },
        { "type": "ROTATE", "angle": 90 },
        { "type": "GRAYSCALE" },
        { "type": "FORMAT", "format": "png" }
    ]
}
```

**Available Transformation Types:**

| Type | Parameters                                     |
|---|------------------------------------------------|
| `RESIZE` | `width` (int), `height` (int)                  |
| `CROP` | `x`, `y`, `width`, `height` (int)              |
| `ROTATE` | `angle` (double)                               |
| `FLIP` | -                                              |
| `MIRROR` | -                                              |
| `GRAYSCALE` | -                                              |
| `SEPIA` | -                                              |
| `WATERMARK` | `text` (String), `position`, `opacity` (float) |
| `COMPRESS` | `quality` (float, 0.0–1.0)                     |
| `FORMAT` | `format` (String, e.g., `"png"`, `"jpg"`)      |

**Response (200 OK):** Raw image bytes with a Content-Type matching the final image format.

## Testing
Execute all tests using:

```bash
./gradlew test
```