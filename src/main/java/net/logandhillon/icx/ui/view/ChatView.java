package net.logandhillon.icx.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.logandhillon.icx.client.ICXClient;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.ui.component.MessageComponent;

public class ChatView extends VBox {
    private static final VBox MESSAGES = new VBox();

    public ChatView() {
        setSpacing(8);

        Label screenName = new Label(ICXClient.getScreenName());
        screenName.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));

        HBox header = new HBox(new VBox(screenName, new Label(ICXClient.getServerAddr().getHostString())));
        header.setPadding(new Insets(16));
        header.setBackground(Background.fill(Color.LIGHTGRAY));

        MESSAGES.setPadding(new Insets(16));
        MESSAGES.setSpacing(8);
        MESSAGES.setAlignment(Pos.BOTTOM_LEFT);

        ScrollPane messageLog = new ScrollPane(MESSAGES);
        messageLog.setMinHeight(384);

        MESSAGES.heightProperty().addListener((_, _, _) -> messageLog.setVvalue(1.0));

        getChildren().addAll(header, messageLog, getMsgBox());
    }

    private static HBox getMsgBox() {
        TextArea msgInp = new TextArea();
        msgInp.setPromptText("Enter a message!");
        msgInp.setPrefHeight(32);

        Button sendBtn = new Button("Send");
        sendBtn.setPrefHeight(36);
        sendBtn.setOnAction(_ -> {
            ICXClient.send(ICXPacket.Command.SEND, msgInp.getText());
            msgInp.clear();
        });

        msgInp.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    msgInp.appendText("\n");    // newline on SHIFT+ENTER
                } else {
                    sendBtn.fire();                // otherwise Send
                    event.consume();
                }
            }
        });

        HBox msgBox = new HBox(msgInp, sendBtn);
        msgBox.setPadding(new Insets(16));
        msgBox.setSpacing(8);
        msgBox.setAlignment(Pos.CENTER_LEFT);
        return msgBox;
    }

    public static void addMessage(String sender, String message) {
        MESSAGES.getChildren().add(new MessageComponent(sender, message));
    }
}
