import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class gameWinnerWindow {
    Stage stage;

    @FXML
    Label winnerNameLabel;
    @FXML
    Label winnerScoreLabel;
    @FXML
    Label winnerLevelLabel;
    @FXML
    Label winnerMoveCountLabel;
    @FXML
    Label Time;
    @FXML
    Button goButton;

    public gameWinnerWindow(GameEngine gameEngine) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameWinnerUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        goButton.setOnMouseClicked(this::OnButtonClick);

        winnerNameLabel.setText("Winner: " + gameEngine.getWinnerName());
        winnerScoreLabel.setText("Score: " + gameEngine.getWinnerScore());
        winnerLevelLabel.setText("Level: " + gameEngine.getWinnerLevel());
        winnerMoveCountLabel.setText("Total Moves: " + gameEngine.getWinnerMoveCount());
        Time.setText("Total time for the game: " + gameEngine.getElapsedTime() + " s");

        stage.showAndWait();
    }

    @FXML
    void OnButtonClick(Event event) {
        try {
            new ScoreboardWindow();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
