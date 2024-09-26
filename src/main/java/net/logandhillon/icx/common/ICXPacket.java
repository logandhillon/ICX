package net.logandhillon.icx.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record ICXPacket(ICXPacket.Command command, SNVS.Token snvs, String content) {
    private static final String VER = "ICX/0.9";

    public enum Command {
        JOIN, EXIT, SEND, UPLOAD,
        SRV_ERR, SRV_HELLO, SRV_KICK
    }

    public static final class ParsingException extends RuntimeException {
        public ParsingException(String reason) {
            super("Failed to parse packet: " + reason);
        }
    }

    public String encode() {
        return Base64.getEncoder().encodeToString(String.format("ICX/0.9 %s$%s$%s", command, snvs, content).getBytes(StandardCharsets.UTF_8));
    }

    public static ICXPacket decode(String b64) throws IllegalArgumentException {
        String s = new String(Base64.getDecoder().decode(b64));

        String[] parts = s.split("\\$", 3);
        String[] brand = parts[0].split(" ");

        if (parts.length < 3 || brand.length < 2) throw new ParsingException("Bad packet structure");
        if (parts[1].isEmpty()) throw new ParsingException("No snvs");
        if (!brand[0].equals(VER))
            throw new ParsingException(String.format("Version mismatch (got %s, expected %s)", brand[0], VER));

        return new ICXPacket(Command.valueOf(brand[1]), SNVS.Token.fromString(parts[1]), parts[2]);
    }

    public ICXPacket stripToken() {
        return new ICXPacket(command, snvs.withoutToken(), content);
    }
}
