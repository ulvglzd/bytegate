# ByteGate

A lightweight HTTP web server framework built from scratch in Java 21 with no web dependencies. Built for learning purposes to demonstrate HTTP internals, concurrency, design patterns, and reflection-based routing.

## Features

- **Raw socket HTTP handling** — parses request lines, headers, and body via `Content-Length`
- **Thread pool with backpressure** — bounded queue, configurable pool size, 503 rejection when exhausted
- **Routing** — exact match and parameterized path variables (`/api/notes/{id}`)
- **Annotation-based controllers** — `@Route` annotation with reflection-based auto-registration
- **Request body parsing** — reads body based on `Content-Length` header
- **Query parameter parsing** — extracts `?key=value` pairs from the URI
- **Configurable logging** — `OFF`, `ERROR`, `INFO`, `DEBUG` levels with timestamp and thread name
- **Graceful shutdown** — JVM shutdown hook with 10-second termination timeout
- **Non-blocking start** — accept loop runs on a dedicated thread

## Quick Start

### Prerequisites

- Java 21+
- Maven

### Build & Run

```bash
mvn compile exec:java -Dexec.mainClass="io.client.Client"
```

### Example Requests

```bash
# Create a note
curl -X POST http://localhost:8080/api/notes -d "My first note"

# Get a note by ID
curl http://localhost:8080/api/notes/1

# Search by keyword
curl http://localhost:8080/api/search?keyword=hello
```

## Usage

### Server Setup

```java
WebServer server = new WebServer.Builder()
        .port(8080)
        .threadPoolSize(10)
        .logLevel(LogLevel.DEBUG)
        .controller(new NoteController(noteService))
        .controller(new KeywordSearchHandler())
        .build();

server.start();
```

### Annotation-Based Controller

```java
public class NoteController {

    @Route(method = "POST", path = "/api/notes")
    public HttpResponse createNote(HttpRequest request) {
        String body = request.bodyPublisher()
                .orElseThrow(() -> new IllegalArgumentException("Body required"))
                .toString();
        // ...
        return HttpResponse.ok("Note created with id: " + id);
    }

    @Route(method = "GET", path = "/api/notes/{id}")
    public HttpResponse getNote(HttpRequest request) {
        String id = request.getPathParam("id");
        // ...
        return HttpResponse.ok(note);
    }
}
```

### Manual Route Registration

```java
new WebServer.Builder()
        .route("GET", "/api/health", request -> HttpResponse.ok("OK"))
        .build();
```

## Project Structure

```
src/main/java/
├── io/bytegate/
│   ├── WebServer.java              # Server entry point with Builder API
│   ├── CoreRequestHandler.java     # Request processing pipeline
│   ├── RequestRouter.java          # Exact + parameterized route matching
│   ├── RouteHandler.java           # Functional interface for handlers
│   ├── RouteMatch.java             # Route resolution result (handler + path params)
│   ├── ThreadPoolManager.java      # Bounded thread pool with rejection handling
│   ├── annotation/
│   │   └── Route.java              # @Route method annotation
│   ├── log/
│   │   ├── LogLevel.java           # OFF, ERROR, INFO, DEBUG
│   │   └── Logger.java             # Formatted console logger
│   ├── model/
│   │   ├── HttpRequest.java        # Request with headers, query/path params, body
│   │   └── HttpResponse.java       # Response with status, headers, body
│   └── util/
│       ├── HttpRequestParser.java  # Parses raw HTTP from InputStream
│       └── HttpResponseWriter.java # Serializes response to OutputStream
└── io/client/                      # Example application
    ├── Client.java                 # Main class — server setup
    ├── NoteController.java         # CRUD controller using @Route
    ├── NoteService.java            # In-memory note storage
    ├── KeywordSearchHandler.java   # Search controller using @Route
    └── FileSearchService.java      # Mock search service
```

## Key Design Decisions

| Area | Approach |
|------|----------|
| HTTP parsing | Manual parsing from raw sockets — no `HttpServer` or Servlet API |
| Concurrency | `ThreadPoolExecutor` with bounded `ArrayBlockingQueue` and 503 on rejection |
| Routing | O(1) exact match via `HashMap`, linear scan for parameterized routes |
| Controllers | `@Route` annotation scanned via reflection at startup |
| Immutability | `HttpRequest` and `HttpResponse` use Builder pattern with unmodifiable maps |
| Shutdown | `shutdown()` + `awaitTermination(10s)` + `shutdownNow()` as fallback |
