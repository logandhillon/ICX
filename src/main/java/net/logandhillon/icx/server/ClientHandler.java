package net.logandhillon.icx.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ClientHandler.class);
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.setName("CONN-" + socket.getInetAddress());
    }

    @Override
    public void run() {
        try {
            LOG.info("Connection successful");

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                LOG.info("Received from client: {}", clientMessage);
                writer.println("Server received: " + clientMessage);
            }

            LOG.info("Disconnected");
        } catch (IOException e) {
            LOG.error("Error handling client", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error("Error closing socket", e);
            }
        }
    }
}