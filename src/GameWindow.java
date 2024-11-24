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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    Label timerLabel;

    @FXML
    Button goButton;

    @FXML
    Button cancelButton;

    @FXML
    MenuItem saveMenuItem;

    @FXML
    MenuItem loadMenuItem;

    @FXML
    TextArea message;

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
        gameEngine.setGameWindow(this);

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

        // Initialize timers before starting the game
//        initGameTimers();

        stage.show();

        if (gameEngine.getPlayerCount() == 1 && !gameEngine.getGameStarted()) {
            goButton.setVisible(true);
            goButton.setDisable(false);
            goButton.setOnMouseClicked(event -> {
                System.out.println("[UI] Button clicked");
                try {
                    OnButtonClick(event);
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to process button click: " + e.getMessage());
                    e.printStackTrace();
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
                numOfPlayersLabel.setText("Number of Players: " + gameEngine.getPlayerCount());
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

    private void initGameTimers() {
        if (gameEngine == null) {
            System.err.println("[ERROR] GameEngine not initialized");
            return;
        }
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Animation logic
                render();
                updateTimerDisplay();
                updateCurrentPlayer();
            }
        };

        moveCheckTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Logic to check moves, game state, etc.
                updateMessage();
            }
        };
    }

    private void gameStart() {
        startTime = System.currentTimeMillis();  // Initialize the start time
        gameEngine.startTimer();  // Start the timer in GameEngine
        animationTimer.start();
        moveCheckTimer.start();
        message.appendText("Message: Game start!\n");
    }

    private void loadImages() throws IOException {
        for (int i = 0; i < symbols.length; i++)
            images[i] = new Image(Files.newInputStream(Paths.get(imagePath + symbols[i] + ".png")));
    }

    private void initCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setFocusTraversable(true); // Ensure the canvas can receive focus

        // Set up key event handler once
        canvas.setOnKeyPressed(event -> {
            try {
                // Only process the key event if the player can move and the game is not over
                if (gameEngine.getCanMove() == 1 && !gameEngine.isGameOver()) {
                    gameEngine.moveMerge(event.getCode().toString());
                    render(); // Refresh the UI after a move
                    // Update UI labels
                    scoreLabel.setText("Score: " + gameEngine.getScore());
                    levelLabel.setText("Level: " + gameEngine.getLevel());
                    comboLabel.setText("Combo: " + gameEngine.getCombo());
                    moveCountLabel.setText("# of Moves: " + gameEngine.getMoveCount());
                }
            } catch (IOException ex) {
                ex.printStackTrace(); // For debugging
                showErrorDialog("An error occurred while processing your move.");
            }
        });

        // AnimationTimer to manage the availability of key presses
        moveCheckTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.getCanMove() != 1 || gameEngine.isGameOver()) {
                    // Disable key presses if the player can't move or the game is over
                    canvas.setOnKeyPressed(null);
                } else {
                    // Ensure the key handler is set if the player can move and the game is not over
                    if (canvas.getOnKeyPressed() == null) {
                        canvas.setOnKeyPressed(event -> {
                            try {
                                gameEngine.moveMerge(event.getCode().toString());
                                render(); // Refresh the UI after a move
                                // Update UI labels
                                scoreLabel.setText("Score: " + gameEngine.getScore());
                                levelLabel.setText("Level: " + gameEngine.getLevel());
                                comboLabel.setText("Combo: " + gameEngine.getCombo());
                                moveCountLabel.setText("# of Moves: " + gameEngine.getMoveCount());
                            } catch (IOException ex) {
                                ex.printStackTrace(); // For debugging
                                showErrorDialog("An error occurred while processing your move.");
                            }
                        });
                    }
                    updateCurrentPlayer();
                }
            }
        };
        moveCheckTimer.start(); // Start the timer

        // Main AnimationTimer for the game loop
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateCurrentPlayer();
                render();
                updateTimerDisplay();
                updateMessage();

                if (gameEngine.isGameOver()) {
                    System.out.println("Game Over!");
                    animationTimer.stop();
                    moveCheckTimer.stop(); // Stop moveCheckTimer as well
                    saveMenuItem.setVisible(false);
                    loadMenuItem.setVisible(false);
                    Platform.runLater(() -> {
                        try {
                            new gameWinnerWindow(gameEngine);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                } else {
                    int movesLeft = gameEngine.getMovesLeft(); // Ensure this method name is correct
                    if (movesLeft == 4) {
                        cancelButton.setVisible(true);
                        cancelButton.setDisable(false);
                    } else if (movesLeft == 0) {
                        cancelButton.setVisible(false);
                        cancelButton.setDisable(true);
                    }
                }
            }
        };
        animationTimer.start(); // Start the main game loop

        // Request focus so that the canvas receives key events
        Platform.runLater(() -> canvas.requestFocus());
    }

    public void render() {
        // Clear existing tiles
        gamePane.getChildren().clear();
        
        int[] board = gameEngine.getBoard();
        System.out.println("[DEBUG] Rendering board: " + Arrays.toString(gameEngine.getBoard()));
        for (int i = 0; i < board.length; i++) {
            int value = board[i];
            if (value > 0) {
                // Create and position the tile
                ImageView tile = new ImageView(images[value - 1]);
                int row = i / SIZE;
                int col = i % SIZE;
                tile.setX(col * TILE_SIZE);
                tile.setY(row * TILE_SIZE);
                gamePane.getChildren().add(tile);
            }
        }
    }

    private void updateMessage(){
        if(gameEngine.getUpdate()){
            message.appendText("Message: " + gameEngine.getUpdatedPlayer() + " just updated the last action!\n");
            gameEngine.setUpdate();
        }else if(gameEngine.getCancel()){
            message.appendText("Message: " + gameEngine.getCancelPlayer() + " just cancelled the last action!\n");
            gameEngine.setCancel();
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

    public void quit() {
        System.out.println("Bye bye!");
        cleanup();
        stage.close();
        System.exit(-1);
    }

    public void cleanup() {
        if (animationTimer != null) animationTimer.stop();
        if (gameTimer != null) gameTimer.stop();
        if (moveCheckTimer != null) gameTimer.stop();
        if (newGameTimer != null) newGameTimer.stop();
    }

    public void setName(String name) {
        nameLabel.setText(name);
        try {
            gameEngine.setPlayerName(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePlayerNumber() {
        numOfPlayersLabel.setText("Number of Players: " + gameEngine.getPlayerCount());
    }

    private void updateCurrentPlayer() {
        currentPlayerLabel.setText("Current Player: " + gameEngine.getCurrentPlayer());
    }

    private void updateTimerDisplay() {
        long elapsedTime = System.currentTimeMillis() - startTime;  // Get elapsed time
        double seconds = elapsedTime / 1000.0;  // Convert milliseconds to seconds
        timerLabel.setText(String.format("Time: %.2f s", seconds));  // Update timer label
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
                        // Ensure timers are initialized before starting the game
                        gameStart();
                        gameTimer.stop();
                        saveMenuItem.setVisible(true);
                        loadMenuItem.setVisible(true);
                        canvas.requestFocus(); // Ensure canvas is focused when the game starts
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

    private void handleError(Exception ex, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(message);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        });
    }

    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}