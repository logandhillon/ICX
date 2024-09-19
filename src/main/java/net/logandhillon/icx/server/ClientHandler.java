package net.logandhillon.icx.server;

import net.logandhillon.icx.common.ICXPacket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ClientHandler.class);
    private final Socket socket;
    private final InetAddress addr;
    private String name;
    private boolean isFresh = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.addr = socket.getInetAddress();
        this.setName("CONN@" + addr);
    }

    private static void sendPacket(PrintWriter writer, ICXPacket packet) {
        writer.println(packet.encode());
    }

    @Override
    public void run() {
        try {
            LOG.info("Connection successful");

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String msg;
            while ((msg = reader.readLine()) != null) {
                try {
                    // TODO: if packet and name ownership valid, ensure packets are broadcasted to all connections
                    //       this way all clients can see what's happening in the chat
                    ICXPacket packet = ICXPacket.decode(msg);

                    // if new connection (fresh), ensure they're registered
                    if (isFresh) {
                        try {
                            if (packet.command() != ICXPacket.Command.JOIN)
                                throw new RuntimeException("You are not registered!");
                            ICXServer.NAME_REGISTRY.registerName(packet.sender(), addr);
                            isFresh = false;
                            this.name = packet.sender();
                        } catch (RuntimeException ex) {
                            sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_ERR, "SERVER", ex.getMessage()));
                            socket.close();
                        }
                    }

                    // throw error if name cannot be verified to that IP
                    if (!this.name.equals(packet.sender()) || !ICXServer.NAME_REGISTRY.verifyName(packet.sender(), addr))
                        throw new RuntimeException("Failed to verify name registration");

                    // TODO: handle all commands
                    switch (packet.command()) {
                        case SEND -> LOG.info("{} said '{}'", packet.sender(), packet.content());
                        case EXIT -> {
                            ICXServer.NAME_REGISTRY.releaseName(this.name);
                            LOG.info("Received EXIT command, closing");
                            socket.close();
                        }
                    }

                    sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_OK, "SERVER", null));
                } catch (RuntimeException e) {
                    sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_ERR, "SERVER", e.getMessage()));
                }
            }

            socket.close();
            LOG.info("Disconnected");
        } catch (SocketException e) {
            LOG.warn("Connection lost or forcibly closed");
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