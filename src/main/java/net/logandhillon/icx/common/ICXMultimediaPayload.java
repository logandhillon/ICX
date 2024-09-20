package net.logandhillon.icx.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public record ICXMultimediaPayload(String filename, FileType fileType, byte[] content) {
    public enum FileType {
        IMAGE("image"),
        VIDEO("video"),
        AUDIO("audio"),
        UNKNOWN("file");

        private final String mimePrefix;

        FileType(String mimePrefix) {
            this.mimePrefix = mimePrefix;
        }

        public static FileType fromMimeType(String mimeType) {
            for (FileType fileType : FileType.values()) {
                if (mimeType != null && mimeType.startsWith(fileType.mimePrefix)) {
                    return fileType;
                }
            }
            return UNKNOWN;
        }
    }

    public static ICXMultimediaPayload fromFile(File file) throws IOException {
        if (!file.isFile() || !file.exists()) throw new FileNotFoundException("does not exist or is not file");
        return new ICXMultimediaPayload(
                file.getName(),
                FileType.fromMimeType(Files.probeContentType(file.toPath())),
                Files.readAllBytes(file.toPath())
        );

    }

    public String encode() {
        return String.format("%s\\%s\\%s", filename, fileType, Base64.getEncoder().encodeToString(content));
    }

    /**
     * @param payload raw multimedia payload
     * @return array of payload parts
     * @throws net.logandhillon.icx.common.ICXPacket.ParsingException invalid/malformed payload
     */
    public static String[] parseOrThrow(String payload) {
        String[] parts = payload.split("\\\\", 3);
        if (parts.length < 3) throw new ICXPacket.ParsingException("Malformed multimedia payload");
        return parts;
    }

    public static ICXMultimediaPayload decode(String payload) {
        String[] parts = parseOrThrow(payload);
        return new ICXMultimediaPayload(parts[0], FileType.valueOf(parts[1]), Base64.getDecoder().decode(parts[2]));
    }
}
