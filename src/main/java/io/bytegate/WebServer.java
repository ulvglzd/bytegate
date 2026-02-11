package io.bytegate;

import io.bytegate.log.LogLevel;
import io.bytegate.log.Logger;
import io.bytegate.model.HttpResponse;
import io.bytegate.util.HttpResponseWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer {

    private final int port;
    private final int threadPoolSize;
    private final RequestRouter router;
    private final Logger logger;

    private ServerSocket serverSocket;
    private ThreadPoolManager poolManager;
    private CoreRequestHandler requestHandler;

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
            requestHandler = new CoreRequestHandler(router, logger);

            registerShutdownHook();
            startAcceptLoop();
        } catch (IOException e) {
            logger.error("Failed to start server: " + e.getMessage());
        }
    }

    private void startAcceptLoop() {
        Thread acceptThread = new Thread(() -> {
            logger.info("Server running on port " + port + "...");
            while (!serverSocket.isClosed()) {
                try {
                    Socket conn = serverSocket.accept();
                    dispatch(conn);
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        logger.error("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        }, "bytegate-accept");
        acceptThread.start();
    }

    private void dispatch(Socket conn) {
        boolean accepted = poolManager.submitTask(() -> {
            try {
                requestHandler.handleRequest(conn);
            } finally {
                closeQuietly(conn);
            }
        });

        if (!accepted) {
            rejectConnection(conn);
        }
    }

    private void rejectConnection(Socket conn) {
        logger.error("Thread pool exhausted, rejecting request with 503");
        try {
            HttpResponseWriter.write(conn.getOutputStream(), HttpResponse.serviceUnavailable());
        } catch (IOException e) {
            logger.error("Failed to send 503 response: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    private void closeQuietly(Socket conn) {
        try {
            conn.close();
        } catch (IOException e) {
            logger.error("Error closing connection: " + e.getMessage());
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "bytegate-shutdown"));
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

        @Deprecated
        public Builder route(String method, String path, RouteHandler handler) {
            router.register(method, path, handler);
            return this;
        }

        public Builder controller(Object controller) {
            ControllerScanner.scan(controller, router);
            return this;
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