package net.logandhillon.icx.ui.component;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MessageComponent extends VBox {
    public MessageComponent(String sender, String message) {
        super();
        Label header = new Label(sender);
        header.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));
        getChildren().addAll(header, new Label(message));
    }
}
