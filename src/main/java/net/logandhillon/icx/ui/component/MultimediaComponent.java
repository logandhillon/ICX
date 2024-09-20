package net.logandhillon.icx.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import net.logandhillon.icx.client.ICXClient;
import net.logandhillon.icx.common.ICXMultimediaPayload;
import net.logandhillon.icx.ui.UI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class MultimediaComponent extends VBox {
    public MultimediaComponent(String sender, ICXMultimediaPayload mmp) {
        super();

        if (!Objects.equals(sender, MessageComponent.lastSender)) {
            Label header = new Label(sender);
            header.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));
            getChildren().addAll(header);
        }

        Pos alignment = sender.equals(ICXClient.getScreenName()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;

        getChildren().add(switch (mmp.fileType()) {
            default -> new FileDownloadComponent(mmp.filename(), mmp.content(), alignment);
        });

        MessageComponent.lastSender = sender;
        setAlignment(alignment);
    }

    private static class FileDownloadComponent extends HBox {
        public FileDownloadComponent(String filename, byte[] content, Pos alignment) {
            super();
            setAlignment(alignment);
            setPadding(new Insets(8));
            setSpacing(32);
            setBackground(Background.fill(Paint.valueOf("#dedede")));
            setMaxWidth(192);

            Button downloadBtn = getDownloadBtn(filename, content);

            Label _filename = new Label(filename);
            _filename.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize()));
            VBox metadata = new VBox(_filename, new Label(formatDataSize(content.length)));
            metadata.setAlignment(Pos.CENTER_LEFT);
            HBox.getHgrow(metadata);

            getChildren().addAll(metadata, downloadBtn);
        }

        private static Button getDownloadBtn(String filename, byte[] content) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Download File");

            Button downloadBtn = new Button("↓");
            downloadBtn.setMinWidth(32);

            downloadBtn.setOnAction(_e -> {
                chooser.setInitialFileName(filename);
                File file = chooser.showSaveDialog(UI.stage);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content);
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Download failed: " + e.getMessage());
                    alert.showAndWait();
                }
            });
            return downloadBtn;
        }
    }

    private static String formatDataSize(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("Bytes cannot be negative");

        String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int unitIdx = 0;

        double size = (double) bytes;

        while (size >= 1024 && unitIdx < units.length - 1) {
            size /= 1024;
            unitIdx++;
        }

        return String.format("%.2f %s", size, units[unitIdx]);
    }
}
