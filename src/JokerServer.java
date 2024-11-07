import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JokerServer {
    ArrayList<Socket> clientList = new ArrayList<>();

    final int[] board = new int[SIZE * SIZE];
    public static final int SIZE = 4;
    private final Map<String, Runnable> actionMap = new HashMap<>();
    private int combo;
    private int level = 1;
    private int score;
    private boolean gameOver;
    private int numOfTilesMoved;

    Random random = new Random(0);
    private String playerName;

    private int totalMoveCount;

    public static final int LIMIT = 14;

    public JokerServer(int port) throws IOException {
        // define a hash map to contain the links from the actions to the corresponding methods
        actionMap.put("U", this::moveUp);
        actionMap.put("D", this::moveDown);
        actionMap.put("L", this::moveLeft);
        actionMap.put("R", this::moveRight);

        nextRound();

        ServerSocket srvSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = srvSocket.accept();

            synchronized (clientList) { //lock the list, when using the muti-thread
                clientList.add(clientSocket); //add the client socket to the client list, after successfully
            }
            Thread t = new Thread(() -> {
                try {
                    serve(clientSocket);  //define in outher thread, not the main thread, so need to handle the IO exception
                } catch (IOException ex) {
                    //ex.printStackTrace(); //debugging only
                    System.out.println("The client is disconnected! " + clientSocket.getInetAddress().toString());

                    synchronized (clientList) {
                        clientList.remove(clientSocket); //remove that socket to the arraylist
                    }
                }
            });
            t.start();   //start the thread to serve multiple client
        }
    }

    public void serve(Socket clientSocket) throws IOException {
        System.out.println("New Connection: ");
        System.out.println(clientSocket.getInetAddress());
        System.out.println(clientSocket.getLocalPort());

        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream _out = new DataOutputStream(clientSocket.getOutputStream());
        sendPuzzle(_out);

        while (true) {
            char dir = (char) in.read();
            System.out.println(dir); //U D L R

            synchronized (clientList) {
                moveMerge("" + dir); //change dir to String

            for (int i : board) //check server really run things
                System.out.print(i + " ");

//            gameOver = !nextRound();

            for(Socket s : clientList) {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.write(dir);   //sending the dir to other clients
                out.flush();    //sending the dir to other clients

                sendPuzzle(out);
                }
            }
        }
    }
    public void sendPuzzle(DataOutputStream out) throws IOException {
        out.write('A');   //going to an array
        out.writeInt(board.length); //send size of the array

        for (int i : board) {
            out.writeInt(i);  //send the values of the array
        }
        out.flush(); //force java to send the data out

    }

    public void moveMerge(String dir) {
        //logic to updating the puzzle, all logic on Server side, but not client side
        synchronized (board) {
            if (actionMap.containsKey(dir)) {
                combo = numOfTilesMoved = 0;

                // go to the hash map, find the corresponding method and call it
                actionMap.get(dir).run();

                // calculate the new score
                score += combo / 5 * 2;

                // determine whether the game is over or not
                if (numOfTilesMoved > 0) {
                    totalMoveCount++;
                    gameOver = level == LIMIT || !nextRound();
                } else
                    gameOver = isFull();

                // update the database if the game is over
                if (gameOver) {
                    try {
                        Database.putScore(playerName, score, level);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean nextRound() {
        if (isFull()) return false;
        int i;

        // randomly find an empty place
        do {
            i = random.nextInt(SIZE * SIZE);
        } while (board[i] > 0);

        // randomly generate a card based on the existing level, and assign it to the select place
        board[i] = random.nextInt(level) / 4 + 1;
        return true;
    }

    private boolean isFull() {
        for (int v : board)
            if (v == 0) return false;
        return true;
    }

    /**
     * move the values downward and merge them.
     */
    private void moveDown() {
        for (int i = 0; i < SIZE; i++)
            moveMerge(SIZE, SIZE * (SIZE - 1) + i, i);
    }

    /**
     * move the values upward and merge them.
     */
    private void moveUp() {
        for (int i = 0; i < SIZE; i++)
            moveMerge(-SIZE, i, SIZE * (SIZE - 1) + i);
    }

    /**
     * move the values rightward and merge them.
     */
    private void moveRight() {
        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
            moveMerge(1, SIZE - 1 + i, i);
    }

    /**
     * move the values leftward and merge them.
     */
    private void moveLeft() {
        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
            moveMerge(-1, i, SIZE - 1 + i);
    }

    /**
     * Move and merge the values in a specific row or column. The moving direction and the specific row or column is determined by d, s, and l.
     * @param d - move distance
     * @param s - the index of the first element in the row or column
     * @param l - the index of the last element in the row or column.
     */
    private void moveMerge(int d, int s, int l) {
        int v, j;
        for (int i = s - d; i != l - d; i -= d) {
            j = i;
            if (board[j] <= 0) continue;
            v = board[j];
            board[j] = 0;
            while (j + d != s && board[j + d] == 0)
                j += d;

            if (board[j + d] == 0) {
                j += d;
                board[j] = v;
            } else {
                while (j != s && board[j + d] == v) {
                    j += d;
                    board[j] = 0;
                    v++;
                    score++;
                    combo++;
                }
                board[j] = v;
                if (v > level) level = v;
            }
            if (i != j)
                numOfTilesMoved++;
        }
    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setPlayerName(String name) {
        playerName = name;
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

    public static void main(String[] args) throws IOException { //handle the exception later
        new JokerServer(12345);
    }
}