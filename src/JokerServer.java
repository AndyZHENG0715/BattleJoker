import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JokerServer {
//    ArrayList<Socket> clientList = new ArrayList<>();
    final static String url = "jdbc:sqlite:data/battleJoker.db";
    static Connection conn;
//    final int[] board = new int[SIZE * SIZE];
//    public static final int SIZE = 4;
//    private final Map<String, Runnable> actionMap = new HashMap<>();
//    private int combo;
//    private int level = 1;
//    private int score;
//    private boolean gameOver;
//    private int numOfTilesMoved;
//    Random random = new Random(0);
//    private String playerName;
//    private int totalMoveCount;
//    public static final int LIMIT = 14;
    private final List<Game> games = new ArrayList<>();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public JokerServer(int port) throws IOException {
        // define a hash map to contain the links from the actions to the corresponding methods
//        actionMap.put("U", this::moveUp);
//        actionMap.put("D", this::moveDown);
//        actionMap.put("L", this::moveLeft);
//        actionMap.put("R", this::moveRight);

//        nextRound();

        ServerSocket srvSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = srvSocket.accept();
            Player player = new Player(clientSocket, "", 0, 0, 1);
            Game assignedGame = null;

            synchronized (games) { //lock the list, when using the muti-thread
                for (Game game : games) {
                    if (game.addPlayer(player)) {
                        assignedGame = game;
                        break;
                    }
                }
                if (assignedGame == null) {
                    assignedGame = new Game();
                    assignedGame.addPlayer(player);
                    games.add(assignedGame);
                }
            }

            Game finalAssignedGame = assignedGame;

            // Add error handling wrapper
            executor.execute(() -> {
                try {
                    System.out.println("[DEBUG] Starting game service for player: " + player.name);
                    finalAssignedGame.serve(player);
                } catch (Exception e) {
                    System.err.println("[ERROR] Game service failed: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            Thread receiverThread = new Thread(() -> {
                try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
                    while (true) {
                        char data = dis.readChar(); // Use readChar for consistency
                        System.out.println("Received command: " + data);
                        handleCommand(data, dis, clientSocket);
                    }
                } catch (IOException ex) {
                    System.err.println("[ERROR] Connection lost with client: " + clientSocket.getRemoteSocketAddress());
                    ex.printStackTrace();
                    // Clean up resources and remove player from game
                    removePlayer(clientSocket);
                }
            });
            receiverThread.start();
        }
    }

    // Additional method to handle different commands
    private void handleCommand(char command, DataInputStream dis, Socket clientSocket) throws IOException {
        switch (command) {
            case 'U':
                // Handle Up move
                processMove("UP", clientSocket);
                break;
            case 'D':
                // Handle Down move
                processMove("DOWN", clientSocket);
                break;
            case 'L':
                // Handle Left move
                processMove("LEFT", clientSocket);
                break;
            case 'R':
                // Handle Right move
                processMove("RIGHT", clientSocket);
                break;
            // Handle other commands
            default:
                System.err.println("[ERROR] Unknown command received: " + command);
        }
    }

    // Ensure proper synchronization when modifying shared resources
    private synchronized void removePlayer(Socket clientSocket) {
        // Logic to remove player from clientList and update game state
    }

    public static void connect() throws SQLException, ClassNotFoundException {
        if (conn == null) {
//            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
        }

    }

    public static void disconnect() throws SQLException {
        if (conn != null)
            conn.close();
    }

    public static ArrayList<HashMap<String, String>> getScores() throws SQLException {
        String sql = "SELECT * FROM scores ORDER BY score DESC LIMIT 10";
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            HashMap<String, String> m = new HashMap<>();
            m.put("name", resultSet.getString("name"));
            m.put("score", resultSet.getString("score"));
            m.put("level", resultSet.getString("level"));
            m.put("time", resultSet.getString("time"));
            data.add(m);
        }
        return data;
    }

    public static void putScore(String name, int score, int level) throws SQLException {
        String sql = String.format("INSERT INTO scores ('name', 'score', 'level', 'time') VALUES ('%s', %d, %d, datetime('now'))", name, score, level);
        Statement statement = conn.createStatement();
        statement.execute(sql);
    }

//    public void serve(Socket clientSocket) throws IOException {
//        System.out.println("New Connection: ");
//        System.out.println(clientSocket.getInetAddress());
//        System.out.println(clientSocket.getLocalPort());
//
//        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
//        playerName = in.readUTF();
//        DataOutputStream _out = new DataOutputStream(clientSocket.getOutputStream());
//        out.writeUTF(playerName); // Send player name to server
//        sendPuzzle(_out);
//
//        while (true) {
//            char dir = (char) in.read();
//            System.out.println(dir); //U D L R
//
//            synchronized (clientList) {
//                moveMerge("" + dir); //change dir to String
//
//            for (int i : board) //check server really run things
//                System.out.print(i + " ");
//
////            gameOver = !nextRound();
//
//            for(Socket s : clientList) {
//                DataOutputStream out = new DataOutputStream(s.getOutputStream());
//                out.write(dir);   //sending the dir to other clients
//                out.flush();    //sending the dir to other clients
//
//                sendPuzzle(out);
//                }
//            }
//        }
//    }
//
//    public void sendPuzzle(DataOutputStream out) throws IOException {
//        out.writeChar('A'); // Indicate data type
//        for (int v : board) {
//            out.writeInt(v);
//        }
//        out.writeBoolean(gameOver);
//        out.writeInt(score);
//        out.writeInt(level);
//        out.writeInt(combo);
//        out.writeInt(totalMoveCount);
//    }
//
//    public void moveMerge(String dir) {
//        //logic to updating the puzzle, all logic on Server side, but not client side
//        synchronized (board) {
//            if (actionMap.containsKey(dir)) {
//                combo = numOfTilesMoved = 0;
//
//                // go to the hash map, find the corresponding method and call it
//                actionMap.get(dir).run();
//
//                // calculate the new score
//                score += combo / 5 * 2;
//
//                // determine whether the game is over or not
//                if (numOfTilesMoved > 0) {
//                    totalMoveCount++;
//                    gameOver = level == LIMIT || !nextRound();
//                } else
//                    gameOver = isFull();
//
//                // update the database if the game is over
//                if (gameOver) {
//                    try {
//                        Database.putScore(playerName, score, level);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    private boolean nextRound() {
//        if (isFull()) return false;
//        int i;
//
//        // randomly find an empty place
//        do {
//            i = random.nextInt(SIZE * SIZE);
//        } while (board[i] > 0);
//
//        // randomly generate a card based on the existing level, and assign it to the select place
//        board[i] = random.nextInt(level) / 4 + 1;
////        return true;
//    }
//
//    private boolean isFull() {
//        for (int v : board)
//            if (v == 0) return false;
//        return true;
//    }
//
//    /**
//     * move the values downward and merge them.
//     */
//    private void moveDown() {
//        for (int i = 0; i < SIZE; i++)
//            moveMerge(SIZE, SIZE * (SIZE - 1) + i, i);
//    }
//
//    /**
//     * move the values upward and merge them.
//     */
//    private void moveUp() {
//        for (int i = 0; i < SIZE; i++)
//            moveMerge(-SIZE, i, SIZE * (SIZE - 1) + i);
//    }
//
//    /**
//     * move the values rightward and merge them.
//     */
//    private void moveRight() {
//        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
//            moveMerge(1, SIZE - 1 + i, i);
//    }
//
//    /**
//     * move the values leftward and merge them.
//     */
//    private void moveLeft() {
//        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
//            moveMerge(-1, i, SIZE - 1 + i);
//    }
//
//    /**
//     * Move and merge the values in a specific row or column. The moving direction and the specific row or column is determined by d, s, and l.
//     * @param d - move distance
//     * @param s - the index of the first element in the row or column
//     * @param l - the index of the last element in the row or column.
//     */
//    private void moveMerge(int d, int s, int l) {
//        int v, j;
//        for (int i = s - d; i != l - d; i -= d) {
//            j = i;
//            if (board[j] <= 0) continue;
//            v = board[j];
//            board[j] = 0;
//            while (j + d != s && board[j + d] == 0)
//                j += d;
//
//            if (board[j + d] == 0) {
//                j += d;
//                board[j] = v;
//            } else {
//                while (j != s && board[j + d] == v) {
//                    j += d;
//                    board[j] = 0;
//                    v++;
//                    score++;
//                    combo++;
//                }
//                board[j] = v;
//                if (v > level) level = v;
//            }
//            if (i != j)
//                numOfTilesMoved++;
//        }
//        if (gameOver) {
//            try {
//                Database.putScore(playerName, score, level);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    public int getValue(int r, int c) {
//        synchronized (board) {
//            return board[r * SIZE + c];
//        }
//    }
//
//    public boolean isGameOver() {
//        return gameOver;
//    }
//
//    public void setPlayerName(String name) {
//        playerName = name;
//    }
//
//    public int getScore() {
//        return score;
//    }
//
//    public int getCombo() {
//        return combo;
//    }
//
//    public int getLevel() {
//        return level;
//    }
//
//    public int getMoveCount() {
//        return totalMoveCount;
//    }

    public static void main(String[] args) throws IOException { //handle the exception later
        new JokerServer(12345);
    }
}