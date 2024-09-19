package net.logandhillon.icx.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ICXServer {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICXServer.class);
    private volatile boolean running = true;

    public void start() {
        int port = 195; // ooh, fun fact port 194 is IRC, so port 195 is a homage to that

        try (ServerSocket server = new ServerSocket(port)) {
            LOG.info("ICX is listening on port {}", port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Shutdown hook triggered. Stopping server...");
                running = false;
                try {
                    server.close();
                } catch (IOException e) {
                    LOG.error("Error closing server socket", e);
                }
            }));

            while (running) {
                try {
                    Socket client = server.accept();
                    if (!running) break;

                    ClientHandler handler = new ClientHandler(client);
                    handler.start();
                } catch (IOException e) {
                    if (running) LOG.error("Server exception: ", e);
                }
            }
            LOG.info("Server stopped");
        } catch (IOException e) {
            LOG.error("Server exception: ", e);
        }
    }
}