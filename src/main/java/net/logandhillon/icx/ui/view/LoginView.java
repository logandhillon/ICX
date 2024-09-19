package net.logandhillon.icx.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.logandhillon.icx.ICX;
import net.logandhillon.icx.client.ICXClient;

import java.io.IOException;
import java.net.InetAddress;

public class LoginView extends VBox {
    private final Label status = new Label();

    public LoginView() {
        setPadding(new Insets(16));
        setSpacing(16);
        setAlignment(Pos.TOP_CENTER);

        Label title = new Label("ICX");
        title.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 36));

        VBox wrapper = getWrapper();
        wrapper.setSpacing(8);
        wrapper.setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(title, new Label("Internet Communication Exchange"), wrapper, status);
    }

    private VBox getWrapper() {
        TextField inpName = new TextField();
        inpName.setPromptText("Screen name");
        inpName.setText(System.getProperty("user.name"));

        TextField inpServerAddr = new TextField();
        inpServerAddr.setPromptText("Server address");

        Button btnJoin = new Button("Join");
        btnJoin.setOnAction(_ -> {
            try {
                if (inpName.getText().isBlank()) {
                    status.setText("Screen name cannot be blank");
                    return;
                }
                ICXClient.connect(inpName.getText(), InetAddress.getByName(inpServerAddr.getText()));
                ICX.stage.close();
                ICX.stage.setScene(new Scene(new ChatView()));
                ICX.stage.show();
            } catch (IOException ex) {
                status.setText(ex.getMessage());
            }
        });

        return new VBox(inpName, inpServerAddr, btnJoin);
    }
}