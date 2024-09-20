package net.logandhillon.icx.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.logandhillon.icx.client.ICXClient;

import java.util.Objects;

public class MessageComponent extends VBox {
    protected static String lastSender = null;

    public MessageComponent(String sender, String message) {
        super();

        if (!Objects.equals(sender, lastSender)) {
            Label header = new Label(sender);
            header.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));
            getChildren().addAll(header);
        }

        getChildren().add(new Label(message));

        if (sender.equals(ICXClient.getScreenName())) setAlignment(Pos.CENTER_RIGHT);
        else setAlignment(Pos.CENTER_LEFT);

        lastSender = sender;
    }
}
