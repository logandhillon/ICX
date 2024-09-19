package net.logandhillon.icx.common;

public record ICXPacket(ICXPacket.Command command, String sender, String content) {
    private static final String VER = "ICX/0.9";

    // TODO: multimedia support when???
    public enum Command {
        JOIN, EXIT, SEND, SRV_ERR, SRV_HELLO, SRV_KICK
    }

    public static final class ParsingException extends RuntimeException {
        public ParsingException(String reason) {
            super("Failed to parse packet: " + reason);
        }
    }

    public String encode() {
        return String.format("ICX/0.9 %s$%s$%s", command, sender, content);
    }

    public static ICXPacket decode(String s) {
        String[] parts = s.split("\\$", 3);
        String[] brand = parts[0].split(" ");
        if (parts.length < 3 || brand.length < 2) throw new ParsingException("Bad packet structure");
        if (parts[1].isEmpty()) throw new ParsingException("No sender");
        if (!brand[0].equals(VER))
            throw new ParsingException(String.format("Version mismatch (got %s, expected %s)", brand[0], VER));
        return new ICXPacket(Command.valueOf(brand[1]), parts[1], parts[2]);
    }
}
