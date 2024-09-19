package net.logandhillon.icx.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.BufferedReader;
import java.io.IOException;

public class S2CHandler extends Thread {
    private static final Logger LOG = LoggerContext.getContext().getLogger(S2CHandler.class);
    private final BufferedReader reader;
    private volatile boolean running = false;

    public S2CHandler(BufferedReader reader) {
        this.reader = reader;
        this.setName("S2C-Handler");
    }

    @Override
    public void run() {
        String msg;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered. Stopping S2C(IN) handler...");
            running = false;
        }));

        while (running) {
            if (!running) break;
            try {
                if ((msg = reader.readLine()) != null) System.out.println(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
