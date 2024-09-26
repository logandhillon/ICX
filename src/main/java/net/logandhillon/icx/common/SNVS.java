package net.logandhillon.icx.common;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Base64;

public class SNVS {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String SEPARATOR = "\u001f";

    /**
     * @return 256-bit (32 char) b64 token
     */
    public static String genToken() {
        byte[] key = new byte[24];  // 24 chars = 32 char b64
        RANDOM.nextBytes(key);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    }

    public record InetToken(InetAddress registrant, String token) {
    }

    public record Token(String name, String token) {
        @Override
        public String toString() {
            return name + SEPARATOR + token;
        }

        public static boolean validate(String snvs) {
            return snvs != null && !snvs.isBlank() && !snvs.contains(SEPARATOR) && !snvs.contains("$");
        }

        public boolean validate() {
            return SNVS.Token.validate(this.name);
        }

        public static Token fromString(String payload) throws IllegalArgumentException {
            String[] snvs = payload.split(SEPARATOR, 2);
            if (snvs[1].contains(SEPARATOR) || snvs[1].contains("$"))
                throw new IllegalArgumentException("Invalid screen name");
            if (snvs[0] == null || snvs[0].isEmpty() || snvs[1].isEmpty())
                throw new IllegalArgumentException("Malformed SNVS");
            return new Token(snvs[0], snvs[1]);
        }

        public Token withoutToken() {
            return new Token(name, null);
        }
    }
}
