package net.logandhillon.icx.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MessageAlertComponent extends HBox {
    public MessageAlertComponent(String message) {
        super();
        Label label = new Label(message);
        label.setFont(Font.font(10));
        label.setTextFill(Color.GRAY);
        setAlignment(Pos.CENTER);
        getChildren().add(label);
    }
}
