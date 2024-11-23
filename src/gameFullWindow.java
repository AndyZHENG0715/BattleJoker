import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class gameFullWindow {
    private Stage stage;

    @FXML
    private Button waitButton;
    @FXML
    private Button leaveButton;
    @FXML
    private Label warningLabel;

    private AnimationTimer animationTimer;
    private GameEngine gameEngine;

    public gameFullWindow(GameEngine gameEngine) throws IOException {
        this.gameEngine = gameEngine;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameFull.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        waitButton.setOnMouseClicked(event -> {
            try {
                OnWaitButtonClick(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        leaveButton.setOnMouseClicked(this::OnLeaveButtonClick);

        initCanvas();
        animationTimer.start();

        stage.showAndWait();
    }

    @FXML
    void OnWaitButtonClick(Event event) throws IOException {
        gameEngine.waitStart();
        waitButton.setVisible(false);
        waitButton.setDisable(true);
        leaveButton.setVisible(false);
        leaveButton.setDisable(true);

        warningLabel.setText("You are queuing, Please Wait!");
    }

    @FXML
    void OnLeaveButtonClick(Event event) {
        stage.close();
        System.exit(0);
    }

    private void initCanvas() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.getNewGame() == 1) {
                    animationTimer.stop(); // Stop the timer to prevent further checks
                    Platform.runLater(() -> stage.close()); // Ensure UI updates are done on the JavaFX Application Thread
                }
            }
        };
    }
}
