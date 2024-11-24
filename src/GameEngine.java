import java.io.*;
import java.net.Socket;

public class GameEngine {
//    Thread receiverThread; // for receiving data sent from the server
    Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;
    //    public static final int LIMIT = 14;
    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
    //    Random random = new Random(0);
    private String serverIP;
    private int serverPort;
    private static GameEngine instance;
    private String playerName;
    private boolean gameOver;
    private int score;
    private int level = 1;
    private int combo;
    private int totalMoveCount;
    private int playerCount;
    private boolean gameStarted;
    public static long startTime;
    public static boolean timerStarted = false;
    private int canMove;
    private int movesLeft;
    private String currentPlayer;
    private String winnerName;
    private int winnerScore;
    private int winnerLevel;
    private int winnerMoveCount;
    private int newGame;
    private String updatePlayer;
    private boolean update = false;
    private String cancelPlayer;
    private boolean cancel = false;

    //    private int numOfTilesMoved;
    //    private final Map<String, Runnable> actionMap = new HashMap<>();

    Thread receiverThread = new Thread(()->{
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            while(true){
                char data = (char) dis.read();
                System.out.println("[NETWORK] Received command: " + data);
                switch (data){
                    case 'A':
                        // download array
                        receiveArray(dis);
                        break;
                    case 'S':
                        receiveScore(dis);
                        break;
                    case 'l':
                        receiveLevel(dis);
                        break;
                    case 'C':
                        receiveCombo(dis);
                        break;
                    case 'M':
                        receiveMove(dis);
                        break;
                    case 'G':
                        receiveGameOver(dis);
                        break;
                    case 'P':
                        receivePlayer(dis);
                        break;
                    case 'T':
                        receiveGameStart(dis);
                        break;
                    case 'Y':
                        receiveCanMove(dis);
                        break;
                    case 'N':
                        receiveMovesLeft(dis);
                        break;
                    case 'Z':
                        receiveCurrentPlayer(dis);
                        break;
                    case 'W':
                        receiveWinner(dis);
                        break;
                    case 'E':
                        receiveNewGame(dis);
                        break;
                    case 'K':
                        receiveUpdatePuzzle(dis);
                        break;
                    case 'B':
                        receiveCancelAction(dis);
                        break;
                    default:
                        // print the direction
                        System.out.println(data);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(); //debugging only, remove it before production
        }
    });

    void receiveCancelAction(DataInputStream dis) throws IOException {
        cancelPlayer = dis.readUTF();
        cancel = true;
        System.out.println(cancelPlayer);
    }

    void receiveUpdatePuzzle(DataInputStream in) throws IOException {
        updatePlayer = in.readUTF();
        update = true;
        System.out.println(updatePlayer);
    }

    void receiveNewGame(DataInputStream in) throws IOException {
        newGame = in.readInt();
        System.out.println(newGame);
    }

    void receiveWinner(DataInputStream in) throws IOException {
        winnerName = in.readUTF();
        winnerScore = in.readInt();
        winnerLevel = in.readInt();
        winnerMoveCount = in.readInt();

        System.out.println(winnerName + ", " + winnerScore + ", " + winnerLevel + ", " + winnerMoveCount );
    }

    void receiveCurrentPlayer(DataInputStream in) throws IOException {
        currentPlayer = in.readUTF();
        System.out.println(currentPlayer);
    }

    void receiveMovesLeft(DataInputStream in) throws IOException {
        movesLeft = in.readInt();
        System.out.println(movesLeft);
    }

    void receiveCanMove(DataInputStream in) throws IOException {
        canMove = in.readInt();
        System.out.println(canMove);
    }

    void receiveGameStart(DataInputStream in) throws IOException {
        int index = in.readInt();
        if(index == 1){
            gameStarted = true;
        }else{
            gameStarted = false;
        }
        System.out.println(index);
    }

    void receivePlayer(DataInputStream in) throws IOException{
        playerCount = in.readInt();
        System.out.println(playerCount);

    }

    void receiveScore(DataInputStream in) throws IOException{
        score = in.readInt();
        System.out.println(score);
    }

    void receiveLevel(DataInputStream in) throws IOException{
        level = in.readInt();
        System.out.println(level);
    }

    void receiveCombo(DataInputStream in) throws IOException{
        combo = in.readInt();
        System.out.println(combo);
    }

    void receiveMove(DataInputStream in) throws IOException{
        totalMoveCount = in.readInt();
        System.out.println(totalMoveCount);
    }

    void receiveGameOver(DataInputStream in) throws IOException{
        int index = in.readInt();
        if(index == 0){
            gameOver = false;
        }else{
            gameOver = true;
        }
        System.out.println(index);
    }

    private GameEngine(String serverIP, int serverPort) throws IOException {
        try {
            clientSocket = new Socket(serverIP, serverPort);
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dis = new DataInputStream(clientSocket.getInputStream());
            receiverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
//        this.serverIP = serverIP;
//        this.serverPort = serverPort;
//        clientSocket = new Socket(serverIP, serverPort);
//        dis = new DataInputStream(clientSocket.getInputStream());
//        dos = new DataOutputStream(clientSocket.getOutputStream());
//
//        receiverThread = new Thread(() -> {
//            try {
//                while (true) { //should not be true here
//                    char data = (char) dis.read();
//                    System.out.println(data);
//                    switch (data) {
//                        case 'A':   //server sent an array
//                            receiveArray(dis);
//                            break;
//                        default:
//                            System.out.println(data);
//                    }
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
//        receiverThread.start();

//        actionMap.put("UP", this::moveUp);
//        actionMap.put("DOWN", this::moveDown);
//        actionMap.put("LEFT", this::moveLeft);
//        actionMap.put("RIGHT", this::moveRight);

        // start the first round
//        nextRound();
        }
    }

    void receiveArray(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        for(int i=0; i<size; i++) {
            board[i] = dis.readInt();
            System.out.print(board[i]);
        }
        System.out.println();
    }

    public static GameEngine getInstance(String IP, int Port) {
        try {
            if (instance == null)
                instance = new GameEngine(IP, Port);
        } catch (IOException e) {
            e.printStackTrace(); // Should handle this more gracefully
            System.exit(-1);  // Abrupt termination
        }
        return instance;
    }

    /**
     * @return true if all blocks are occupied.
     */
//    private boolean isFull() {
//        for (int v : board)
//            if (v == 0) return false;
//        return true;
//    }

    public void waitStart() throws IOException{
        dos.write('W');
        dos.flush();
    }

    /**
     * Move and combine the cards based on the input direction
     *
     */

    public void moveMerge(String dir) throws IOException {
        System.out.println("[DEBUG] Sending move command: " + dir);
        dos.writeByte('M');  // Single byte command identifier
        dos.writeChar(dir.charAt(0));  // Consistent character encoding
        dos.flush();
        System.out.println("[DEBUG] Move command sent successfully");
    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setPlayerName(String name) throws IOException {
        playerName = name;
        dos.writeUTF("Player Name");
        dos.writeUTF(playerName);
        dos.flush();
    }

    public static void startTimer() {
        startTime = System.currentTimeMillis();
        timerStarted = true;
    }

    public static double getElapsedTime() {
        if (!timerStarted) {
            return 0;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        return (double) elapsedTime / 1000;
    }

    public static void stopTimer() {
        timerStarted = false;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public int getLevel() {
        return level;
    }

    public int getMoveCount() {
        return totalMoveCount;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getWinnerScore() {
        return winnerScore;
    }

    public int getWinnerLevel() {
        return winnerLevel;
    }

    public int getWinnerMoveCount() {
        return winnerMoveCount;
    }

    public int getNewGame() {
        return newGame;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) throws IOException {
        this.gameStarted = gameStarted;
        dos.writeUTF("Game Start");
        dos.flush();
    }

    public int getCanMove() {
        return canMove;
    }

    public String getUpdatedPlayer() {
        return updatePlayer;
    }

    public void setUpdate() {
        this.update = false;
    }

    public void setCancel() {
        this.cancel = false;
    }

    public boolean getUpdate() {
        return update;
    }

    public String getCancelPlayer() {
        return cancelPlayer;
    }

    public boolean getCancel() {
        return cancel;
    }

    public void cancelAction() throws IOException {
        dos.writeUTF("Cancel Last Action");
        dos.flush();
    }

    public void updatePuzzle() throws IOException {
        dos.write('K');
        dos.flush();
    }

    public void newGame() throws IOException {
        dos.write('E');
        dos.flush();
    }

    public void getWinner() throws IOException {
        dos.write('W');
        dos.flush();
    }

    public void savePuzzle(File file) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            synchronized (board) {
                dos.writeInt(SIZE);
                for (int value : board) {
                    dos.writeInt(value);
                }
                dos.writeUTF(currentPlayer);
                dos.writeInt(level);
                dos.writeInt(score);
                dos.writeInt(combo);
                dos.writeInt(totalMoveCount);
                dos.writeBoolean(gameOver);
                dos.writeInt(playerCount);
                dos.writeBoolean(gameStarted);
            }
        }
    }

    public void loadPuzzle(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            synchronized (board) {
                int newSize = dis.readInt();
                if (newSize != SIZE) {
                    throw new IOException("Invalid puzzle size");
                }
                for (int i = 0; i < board.length; i++) {
                    board[i] = dis.readInt();
                }
                currentPlayer = dis.readUTF();
                level = dis.readInt();
                score = dis.readInt();
                combo = dis.readInt();
                totalMoveCount = dis.readInt();
                gameOver = dis.readBoolean();
                playerCount = dis.readInt();
                gameStarted = dis.readBoolean();
            }
            updateGameState();
        }
    }

    public void uploadPuzzleToServer(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            dos.writeUTF("Upload Puzzle");
            dos.writeInt(SIZE);
            for (int value : board) {
                dos.writeInt(value);
            }
            dos.writeUTF(currentPlayer);
            dos.writeInt(level);
            dos.writeInt(score);
            dos.writeInt(combo);
            dos.writeInt(totalMoveCount);
            dos.writeBoolean(gameOver);
            dos.writeInt(playerCount);
            dos.writeBoolean(gameStarted);
            dos.flush();
        }
    }

    private void updateGameState() {
        updateCurrentPlayer();
        updateBoard();
        updateScore();
        updateLevel();
        updateCombo();
        updateMoveCount();
        updateGameOver();
        updatePlayerCount();
        updateGameStarted();
    }

    private void updateCurrentPlayer() {
        System.out.println("Current Player(Updated): " + currentPlayer);
    }

    private void updateBoard() {
        System.out.print("Board(Updated): ");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(getValue(i, j) + " ");
            }
            System.out.println();
        }
    }

    private void updateScore() {
        System.out.println("Score(Updated): " + score);
    }

    private void updateLevel() {
        System.out.println("Level(Updated): " + level);
    }

    private void updateCombo() {
        System.out.println("Combo(Updated): " + combo);
    }

    private void updateMoveCount() {
        System.out.println("Move Count(Updated): " + totalMoveCount);
    }

    private void updateGameOver() {
        System.out.println("Game Over Status(Updated): " + gameOver);
    }

    private void updatePlayerCount() {
        System.out.println("Player Count(Updated): " + playerCount);
    }

    private void updateGameStarted() {
        System.out.println("Game Started Status(Updated): " + gameStarted);
    }
}