package io.bytegate;

import io.bytegate.annotation.Route;
import io.bytegate.log.LogLevel;
import io.bytegate.log.Logger;
import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;
import io.bytegate.util.HttpResponseWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer {

    private final int port;
    private final int threadPoolSize;
    private final RequestRouter router;
    private final Logger logger;

    private ServerSocket serverSocket;
    private ThreadPoolManager poolManager;

    private WebServer(int port, int threadPoolSize, RequestRouter router, Logger logger) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.router = router;
        this.logger = logger;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            poolManager = ThreadPoolManager.create(threadPoolSize);
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "bytegate-shutdown"));

            CoreRequestHandler requestHandler = new CoreRequestHandler(router, logger);

            Thread acceptThread = new Thread(() -> {
                logger.info("Server running on port " + port + "...");
                while (!serverSocket.isClosed()) {
                    try {
                        Socket conn = serverSocket.accept();
                        processRequest(poolManager, requestHandler, conn);
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            logger.error("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            }, "bytegate-accept");
            acceptThread.start();

        } catch (IOException e) {
            logger.error("Failed to start server: " + e.getMessage());
        }
    }

    public void stop() {
        logger.info("Shutting down...");
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket: " + e.getMessage());
            }
        }
        if (poolManager != null) {
            poolManager.close();
        }
        logger.info("Server stopped");
    }

    private void processRequest(ThreadPoolManager poolManager, CoreRequestHandler requestHandler, Socket conn) {
        boolean accepted = poolManager.submitTask(() -> {
            try {
                requestHandler.handleRequest(conn);
            } finally {
                try {
                    conn.close();
                } catch (IOException e) {
                    logger.error("Error closing connection: " + e.getMessage());
                }
            }
        });

        if (!accepted) {
            logger.error("Thread pool exhausted, rejecting request with 503");
            try {
                HttpResponseWriter.write(conn.getOutputStream(), HttpResponse.serviceUnavailable());
            } catch (IOException e) {
                logger.error("Failed to send 503 response: " + e.getMessage());
            } finally {
                try {
                    conn.close();
                } catch (IOException e) {
                    logger.error("Error closing rejected connection: " + e.getMessage());
                }
            }
        }
    }

    public static class Builder {
        private int port = 8080;
        private int threadPoolSize = 10;
        private LogLevel logLevel = LogLevel.INFO;
        private final RequestRouter router = new RequestRouter();

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder route(String method, String path, RouteHandler handler) {
            router.register(method, path, handler);
            return this;
        }

        public Builder controller(Object controller) {
            for (Method method : controller.getClass().getDeclaredMethods()) {
                Route route = method.getAnnotation(Route.class);
                if (route == null) {
                    continue;
                }

                validateMethod(method);

                method.setAccessible(true);
                RouteHandler handler = (request) -> {
                    try {
                        return (HttpResponse) method.invoke(controller, request);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke @Route method: " + method.getName(), e);
                    }
                };
                router.register(route.method(), route.path(), handler);
            }
            return this;
        }

        private void validateMethod(Method method) {
            if (method.getParameterCount() != 1
                    || method.getParameterTypes()[0] != HttpRequest.class
                    || method.getReturnType() != HttpResponse.class) {
                throw new IllegalArgumentException(
                        "@Route method must have signature: HttpResponse methodName(HttpRequest). "
                                + "Invalid method: " + method.getName());
            }
        }

        public Builder withDefaultParameters() {
            this.port = 8080;
            this.threadPoolSize = 10;
            return this;
        }

        public WebServer build() {
            return new WebServer(port, threadPoolSize, router, new Logger(logLevel));
        }
    }

}