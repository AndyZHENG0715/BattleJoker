import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameEngine {
    Thread receiverThread; // for receiving data sent from the server
    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    //    public static final int LIMIT = 14;
    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
    //    Random random = new Random(0);
    private String serverIP;
    private int serverPort;
    private static GameEngine instance;
    //    private String playerName;
    private boolean gameOver;
    private int score;
    private int level = 1;
    private int combo;
    private int totalMoveCount;
    //    private int numOfTilesMoved;
    //    private final Map<String, Runnable> actionMap = new HashMap<>();

    private GameEngine(String serverIP, int serverPort) throws IOException {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        clientSocket = new Socket(serverIP, serverPort);
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        receiverThread = new Thread(() -> {
            try {
                while (true) { //should not be true here
                    char data = (char) in.read();
                    System.out.println(data);
                    switch (data) {
                        case 'A':   //server sent an array
                            receiveArray(in);
                            break;
                        default:
                            System.out.println(data);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        receiverThread.start();

//        actionMap.put("UP", this::moveUp);
//        actionMap.put("DOWN", this::moveDown);
//        actionMap.put("LEFT", this::moveLeft);
//        actionMap.put("RIGHT", this::moveRight);

        // start the first round
//        nextRound();
    }

    public void receiveArray(DataInputStream in) throws IOException {
        for (int i = 0; i < board.length; i++) {
            board[i] = in.readInt();
        }
        gameOver = in.readBoolean();
        score = in.readInt();
        level = in.readInt();
        combo = in.readInt();
        totalMoveCount = in.readInt();
    }

    public static GameEngine getInstance(String serverIP, int serverPort) {
        if (instance == null) {
            try {
                instance = new GameEngine(serverIP, serverPort);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
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

    /**
     * Move and combine the cards based on the input direction
     *
     */

    public void moveMerge(String dir) throws IOException { //whatever you move the puzzle
        System.out.println(dir);
        out.write(dir.charAt(0));//send out the first char only
        out.flush(); //force output
        //send the direction to the sever
    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

//    public void setPlayerName(String name) {
//        playerName = name;
//    }

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
}