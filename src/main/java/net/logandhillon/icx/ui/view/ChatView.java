package net.logandhillon.icx.ui.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import net.logandhillon.icx.client.ICXClient;
import net.logandhillon.icx.common.ICXMultimediaPayload;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.ui.UI;
import net.logandhillon.icx.ui.component.MessageAlertComponent;
import net.logandhillon.icx.ui.component.MessageComponent;
import net.logandhillon.icx.ui.component.MultimediaComponent;

import java.io.File;
import java.io.IOException;

public class ChatView extends VBox {
    private static final VBox MESSAGES = new VBox();
    private static final Label ROOM_NAME = new Label();

    public ChatView() {
        setSpacing(8);
        setFillWidth(true);

        Label screenName = new Label(ICXClient.getScreenName());
        screenName.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));

        HBox header = getHeader(screenName);
        header.setPadding(new Insets(16));
        header.setBackground(Background.fill(Color.LIGHTGRAY));

        MESSAGES.setPadding(new Insets(16, 32, 16, 16));
        MESSAGES.setSpacing(8);
        MESSAGES.prefWidthProperty().bind(this.widthProperty());

        ScrollPane messageLog = new ScrollPane(MESSAGES);
        messageLog.setMinHeight(384);
        messageLog.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageLog.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        MESSAGES.heightProperty().addListener((_1, _2, _3) -> messageLog.setVvalue(1.0));

        getChildren().addAll(header, messageLog, getMsgBox());
    }

    public static void exitRoom() {
        try {
            ICXClient.disconnect();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to disconnect: " + e.getMessage());
            alert.showAndWait();
        }
        MESSAGES.getChildren().clear();
    }

    private static HBox getHeader(Label screenName) {
        Button leaveBtn = new Button("Exit");
        leaveBtn.setOnAction(_e -> UI.reloadScene(new Scene(new LoginView()), ChatView::exitRoom));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        updateRoomName();

        return new HBox(new VBox(screenName, ROOM_NAME), spacer, leaveBtn);
    }

    private static HBox getMsgBox() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select file to upload");

        Button uploadBtn = new Button("+");
        uploadBtn.setOnAction(_e -> {
            File f = chooser.showOpenDialog(UI.stage);
            ICXClient.uploadFile(f);
        });

        TextField msgInp = new TextField();
        msgInp.setPromptText("Enter a message!");
        msgInp.setPrefWidth(384);

        Button sendBtn = new Button("Send");
        sendBtn.setMinWidth(64);
        sendBtn.setOnAction(_e -> {
            ICXClient.send(ICXPacket.Command.SEND, msgInp.getText());
            msgInp.clear();
        });

        msgInp.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendBtn.fire();
                event.consume();
            }
        });

        HBox msgBox = new HBox(uploadBtn, msgInp, sendBtn);
        msgBox.setPadding(new Insets(16));
        msgBox.setSpacing(8);
        msgBox.setAlignment(Pos.CENTER_LEFT);
        return msgBox;
    }

    public static void postMessage(String sender, String message) {
        MESSAGES.getChildren().add(new MessageComponent(sender, message));
    }

    public static void postMMP(String sender, ICXMultimediaPayload payload) {
        MESSAGES.getChildren().add(new MultimediaComponent(sender, payload));
    }

    public static void postAlert(String alert) {
        MESSAGES.getChildren().add(new MessageAlertComponent(alert));
    }

    public static void updateRoomName() {
        Platform.runLater(() -> ROOM_NAME.setText(ICXClient.connectedRoomName != null ? ICXClient.connectedRoomName : ICXClient.getServerAddr().getHostString()));
    }
}
