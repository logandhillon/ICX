package net.logandhillon.icx.server;

import net.logandhillon.icx.common.ICXMultimediaPayload;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.common.SNVS;
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
    private SNVS.Token snvs;
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

            sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_HELLO,
                    NameRegistry.SERVER,
                    ICXServer.PROPERTIES.roomName() + "\035" + ChatLogger.encodeLogs()));

            String msg;
            while ((msg = reader.readLine()) != null) {
                try {
                    ICXPacket packet = ICXPacket.decode(msg);
                    LOG.debug("Received {} packet", packet.command());

                    // if new connection (fresh), ensure they're registered
                    if (isFresh) {
                        try {
                            if (packet.command() != ICXPacket.Command.JOIN)
                                throw new RuntimeException("You are not registered!");
                            ICXServer.NAME_REGISTRY.registerName(packet.snvs(), addr);
                            ICXServer.CLIENT_WRITERS.add(writer);
                            isFresh = false;
                            this.snvs = packet.snvs();
                        } catch (Exception ex) {
                            sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_KICK, NameRegistry.SERVER, ex.getMessage()));
                            socket.close();
                        }
                    }

                    // throw error if name cannot be verified to that IP
                    if (!this.snvs.equals(packet.snvs()) || !ICXServer.NAME_REGISTRY.verifyName(packet.snvs(), addr))
                        throw new RuntimeException("SNVS failed");

                    switch (packet.command()) {
                        case SEND -> {
                            if (packet.content().isBlank())
                                throw new RuntimeException("Message content cannot be blank");
                            ChatLogger.log(packet.snvs().name(), packet.content());
                        }
                        case FILE_INF -> {
                            ICXMultimediaPayload.parseOrThrow(packet.content()); // verify packet integrity or throw
                            ChatLogger.log(packet.snvs().name(), "[ Media unavailable ]");
                        }
                        case JOIN -> ChatLogger.logAlert(String.format("Welcome, %s!", packet.snvs().name()));
                        case EXIT -> {
                            ChatLogger.logAlert(String.format("Farewell, %s!", packet.snvs().name()));
                            LOG.debug("Received EXIT command");
                            socket.close();
                        }
                        case SRV_ERR -> throw new RuntimeException("Illegal command");
                    }

                    ICXServer.broadcast(packet.stripToken());
                } catch (Exception e) {
                    sendPacket(writer, new ICXPacket(ICXPacket.Command.SRV_ERR, NameRegistry.SERVER, e.getMessage()));
                }
            }
        } catch (SocketException e) {
            LOG.warn("Connection lost or forcibly closed: {}", e.getMessage());
        } catch (IOException e) {
            LOG.error("Error handling client: {}", e.getMessage());
        } finally {
            try {
                ICXServer.NAME_REGISTRY.releaseName(this.snvs);
                ICXServer.CLIENT_WRITERS.remove(this.writer);
                ICXServer.broadcast(new ICXPacket(ICXPacket.Command.EXIT, this.snvs, null));
                socket.close();
                LOG.info("Disconnected");
            } catch (IOException e) {
                LOG.error("Error closing socket", e);
            }
        }
    }
}