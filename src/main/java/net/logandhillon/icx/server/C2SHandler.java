package net.logandhillon.icx.server;

import net.logandhillon.icx.common.ICXPacket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class C2SHandler extends Thread {
    private static final Logger LOG = LoggerContext.getContext().getLogger(C2SHandler.class);
    private final Socket socket;
    private final InetAddress addr;
    private PrintWriter writer;
    private String sender;
    private boolean isFresh = true;

    public C2SHandler(Socket socket) {
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
            writer = new PrintWriter(output, true);

            sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_HELLO, "SERVER", "Connected"));

            String msg;
            while ((msg = reader.readLine()) != null) {
                try {
                    ICXPacket packet = ICXPacket.decode(msg);
                    LOG.info("Received {} packet", packet.command());

                    // if new connection (fresh), ensure they're registered
                    if (isFresh) {
                        try {
                            if (packet.command() != ICXPacket.Command.JOIN)
                                throw new RuntimeException("You are not registered!");
                            ICXServer.NAME_REGISTRY.registerName(packet.sender(), addr);
                            ICXServer.CLIENT_WRITERS.add(writer);
                            isFresh = false;
                            this.sender = packet.sender();
                        } catch (RuntimeException ex) {
                            sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_KICK, "SERVER", ex.getMessage()));
                            socket.close();
                        }
                    }

                    // throw error if name cannot be verified to that IP
                    if (!this.sender.equals(packet.sender()) || !ICXServer.NAME_REGISTRY.verifyName(packet.sender(), addr))
                        throw new RuntimeException("Failed to verify name registration");

                    switch (packet.command()) {
                        case SEND -> LOG.info("{}: '{}'", packet.sender(), packet.content());
                        case EXIT -> {
                            LOG.info("Received EXIT command");
                            socket.close();
                        }
                        case SRV_ERR -> throw new RuntimeException("Illegal command");
                    }

                    ICXServer.broadcast(packet);
                } catch (RuntimeException e) {
                    sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_ERR, "SERVER", e.getMessage()));
                }
            }
        } catch (SocketException e) {
            LOG.warn("Connection lost or forcibly closed: {}", e.getMessage());
        } catch (IOException e) {
            LOG.error("Error handling client", e);
        } finally {
            try {
                ICXServer.NAME_REGISTRY.releaseName(this.sender);
                ICXServer.CLIENT_WRITERS.remove(this.writer);
                ICXServer.broadcast(new ICXPacket(ICXPacket.Command.EXIT, this.sender, null));
                socket.close();
                LOG.info("Disconnected");
            } catch (IOException e) {
                LOG.error("Error closing socket", e);
            }
        }
    }
}