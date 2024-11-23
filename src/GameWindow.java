import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameWindow {
    @FXML
    MenuBar menuBar;

    @FXML
    Label nameLabel;

    @FXML
    Label scoreLabel;

    @FXML
    Label levelLabel;

    @FXML
    Label comboLabel;

    @FXML
    Label moveCountLabel;

    @FXML
    Pane boardPane;

    @FXML
    Canvas canvas;

    @FXML
    Label numOfPlayersLabel;

    @FXML
    Label currentPlayerLabel;

    @FXML
    timerLabel;

    @FXML
    Button goButton;

    @FXML
    Button cancelButton;

    @FXML
    MenuItem saveMenuItem;

    @FXML
    MenuItem loadMenuItem;

    @FXML
    TextArea TextArea;

    long startTime = 0;
    Stage stage;
    AnimationTimer animationTimer;
    AnimationTimer gameTimer;
    AnimationTimer moveCheckTimer;
    AnimationTimer newGameTimer;

    final String imagePath = "images/";
    final String[] symbols = {"bg", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "Joker"};
    final Image[] images = new Image[symbols.length];
    GameEngine gameEngine;

    public GameWindow(Stage stage, String serverIP, int serverPort) throws IOException {
        loadImages();

        gameEngine = GameEngine.getInstance(serverIP, serverPort); // Initialize GameEngine

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found");
        }
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        this.stage = stage;

        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        stage.widthProperty().addListener(w -> onWidthChangedWindow(((ReadOnlyDoubleProperty) w).getValue()));
        stage.heightProperty().addListener(h -> onHeightChangedWindow(((ReadOnlyDoubleProperty) h).getValue()));
        stage.setOnCloseRequest(event -> quit());

        saveMenuItem.setOnAction(event -> savePuzzle());
        loadMenuItem.setOnAction(event -> loadPuzzle());
        message.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            event.consume();
        });
        message.textProperty().addListener((observable, oldValue, newValue) -> {
            message.setScrollTop(Double.MAX_VALUE); // Scroll to the bottom
        });
        cancelButton.setOnAction(event -> {
            try {
                cancelAction(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

//        gameEngine = GameEngine.getInstance(serverIP, serverPort); // Initialize GameEngine

        if (gameEngine.getPlayerCount() == 1 && !gameEngine.getGameStarted()) {
            goButton.setVisible(true);
            goButton.setDisable(false);
            goButton.setOnMouseClicked(event -> {
                try {
                    OnButtonClick(event);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            waitGameStart();
        } else if (gameEngine.getPlayerCount() == 2 || gameEngine.getPlayerCount() == 3) {
            if (!gameEngine.getGameStarted()) {
                waitGameStart();
            } else {
                waitNewGame();
            }
        } else if (gameEngine.getPlayerCount() == 4) {
            if (!gameEngine.getGameStarted()) {
                numberofPlayerLabel.setText("Number of Players: " + gameEngine.getPlayerCount());
                gameEngine.setGameStarted(true);
                initCanvas();
                gameStart();

                saveMenuItem.setVisible(true);
                loadMenuItem.setVisible(true);
            } else {
                waitNewGame();
            }
        } else if (gameEngine.getPlayerCount() >= 5 || gameEngine.getGameStarted()) {
            waitNewGame();
        }
    }


    private void gameStart() {
        animationTimer.start();
        startTime = System.currentTimeMillis();  // Initialize the start time
        gameEngine.startTimer();  // Start the timer in GameEngine
        moveCheckTimer.start();
        message.appendText("Message: Game Started\n");
    }

    private void loadImages() throws IOException {
        for (int i = 0; i < symbols.length; i++)
            images[i] = new Image(Files.newInputStream(Paths.get(imagePath + symbols[i] + ".png")));
    }

    private void initCanvas() {
        canvas.setOnKeyPressed(event -> {
            try {
                gameEngine.moveMerge(event.getCode().toString());
            } catch (IOException ex) {
                ex.printStackTrace(); //debug
                System.exit(-1); //cannot just end it, project need to show a box or a window to end it
            }
//            scoreLabel.setText("Score: " + gameEngine.getScore());
//            levelLabel.setText("Level: " + gameEngine.getLevel());
//            comboLabel.setText("Combo: " + gameEngine.getCombo());
//            moveCountLabel.setText("# of Moves: " + gameEngine.getMoveCount());
        });

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
                if (gameEngine.isGameOver()) {
                    System.out.println("Game Over!");
                    animationTimer.stop();

                    Platform.runLater(() -> {
                        try {
                            new ScoreboardWindow();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                }
            }
        };
        canvas.requestFocus();
    }

    private void render() {
        if (gameEngine == null) return; // Ensure gameEngine is not null

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double sceneSize = Math.min(w, h);
        double blockSize = sceneSize / GameEngine.SIZE;
        double padding = blockSize * .05;
        double startX = (w - sceneSize) / 2;
        double startY = (h - sceneSize) / 2;
        double cardSize = blockSize - (padding * 2);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        double y = startY;
        int v;

        // Draw the background and cards from left to right, and top to bottom.
        for (int i = 0; i < GameEngine.SIZE; i++) {
            double x = startX;
            for (int j = 0; j < GameEngine.SIZE; j++) {
                gc.drawImage(images[0], x, y, blockSize, blockSize);  // Draw the background

                v = gameEngine.getValue(i, j); // v = the values(card number) sent by server

                if (v > 0)  // if a card is in the place, draw it
                    gc.drawImage(images[v], x + padding, y + padding, cardSize, cardSize);

                x += blockSize;
            }
            y += blockSize;
        }
    }

    void OnButtonClick(Event event) throws IOException {
        goButton.setVisible(false);
        goButton.setDisable(true);
        gameEngine.setGameStarted(true);
    }

    private void cancelAction(Event event) throws IOException {
        cancelButton.setVisible(false);
        cancelButton.setDisable(true);
        gameEngine.cancelAction();
    }

    void onWidthChangedWindow(double w) {
        double width = w - boardPane.getBoundsInParent().getMinX();
        boardPane.setMinWidth(width);
        canvas.setWidth(width);
        render();
    }

    void onHeightChangedWindow(double h) {
        double height = h - boardPane.getBoundsInParent().getMinY() - menuBar.getHeight();
        boardPane.setMinHeight(height);
        canvas.setHeight(height);
        render();
    }

    void quit() {
        System.out.println("Bye bye");
        stage.close();
        System.exit(0);
    }

    public void setName(String name) {
        nameLabel.setText(name);
//        gameEngine.setPlayerName(name);
    }

    private void updatePlayerNumber() {
        numberofPlayerLabel.setText("Number of Players: " + gameEngine.getPlayerCount());
    }

    private void savePuzzle() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Puzzle");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Puzzle Files", "*.pzl"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                gameEngine.savePuzzle(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPuzzle() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Puzzle");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Puzzle Files", "*.pzl"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                gameEngine.loadPuzzle(file);
                gameEngine.uploadPuzzleToServer(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitGameStart() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayerNumber();
                if (gameEngine.getPlayerCount() == 4 || gameEngine.getGameStarted()) {
                    goButton.setVisible(false);
                    goButton.setDisable(true);
                    Platform.runLater(() -> {
                        initCanvas();
                        gameStart();
                        gameTimer.stop(); // Stop the timer once the game starts

                        saveMenuItem.setVisible(true);
                        loadMenuItem.setVisible(true);
                    });
                }
            }
        };
        gameTimer.start(); // Start the timer after it's initialized
    }

    private void waitNewGame() throws IOException {
        new gameFullWindow(gameEngine);

        newGameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.getNewGame() == 1) {
                    Platform.runLater(() -> {
                        waitGameStart();
                        newGameTimer.stop(); // Stop the timer once the game starts
                    });
                }
            }
        };
        newGameTimer.start();
    }
}