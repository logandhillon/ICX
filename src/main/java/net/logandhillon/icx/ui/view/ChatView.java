package net.logandhillon.icx.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.logandhillon.icx.client.ICXClient;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.ui.component.MessageComponent;

public class ChatView implements UIView<VBox> {
    private final VBox parent;
    private final VBox log;

    public ChatView() {
        parent = new VBox();
        parent.setSpacing(8);

        Label screenName = new Label(ICXClient.getScreenName());
        screenName.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));

        HBox header = new HBox(new VBox(screenName, new Label(ICXClient.getServerAddr().getHostString())));
        header.setPadding(new Insets(16));
        header.setBackground(Background.fill(Color.LIGHTGRAY));

        log = new VBox();
        log.setPadding(new Insets(16));
        log.setSpacing(8);

        parent.getChildren().addAll(header, log, getMsgBox());
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

        HBox msgBox = new HBox(msgInp, sendBtn);
        msgBox.setPadding(new Insets(16));
        msgBox.setSpacing(8);
        msgBox.setAlignment(Pos.CENTER_LEFT);
        return msgBox;
    }

    public void addMessage(String sender, String message) {
        log.getChildren().add(new MessageComponent(sender, message));
    }

    @Override
    public VBox getView() {
        return parent;
    }
}
