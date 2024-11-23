import java.net.Socket;

public class Player {
    Socket socket;
    String name;
    int score;
    int totalMoveCount;
    int level;
    boolean skillUsed = false;
    int[] previousBoard;
    int previousScore;
    int previousLevel;
    int previousCombo;
    int previousTotalMoveCount;


    public Player(Socket socket, String name, int score, int totalMoveCount, int level) {
        this.socket = socket;
        this.name = name;
        this.score = score;
        this.totalMoveCount = totalMoveCount;
        this.level = level;
    }

}
