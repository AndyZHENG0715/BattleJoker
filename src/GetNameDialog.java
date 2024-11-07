import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class GetNameDialog {
    @FXML
    TextField nameField;

    @FXML
    TextField ipField;

    @FXML
    TextField portField;

    @FXML
    Button goButton;

    Stage stage;
    String playerName;
    String serverIP;
    int serverPort;

    public GetNameDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("getNameUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        goButton.setOnMouseClicked(this::onButtonClick);

        stage.showAndWait();
    }

    @FXML
    void onButtonClick(Event event) {
        playerName = nameField.getText().trim();
        serverIP = ipField.getText().trim();
        serverPort = Integer.parseInt(portField.getText().trim());
        if (!playerName.isEmpty() && !serverIP.isEmpty() && serverPort > 0) {
            stage.close();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }
}