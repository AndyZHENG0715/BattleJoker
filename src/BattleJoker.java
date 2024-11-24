import javafx.application.Application;
import javafx.stage.Stage;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BattleJoker extends Application {
//    Socket clientSocket;

    @Override
    public void start(Stage primaryStage) { //starting point of application
        try {
            GetNameDialog dialog = new GetNameDialog();
            String serverIP = dialog.getServerIP();
            int serverPort = dialog.getServerPort();
            GameWindow win = new GameWindow(primaryStage, serverIP, serverPort);
            win.setName(dialog.getPlayerName());
            JokerServer.connect();
//            clientSocket = new Socket("127.0.0.1", 12345); //change this hard code later
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void stop() {
        try {
            JokerServer.disconnect();
        } catch (SQLException ignored) {
        }
    }

    private void sendMoveCommand(String direction) {
        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeChar(direction.charAt(0)); // Use writeChar for consistency
            dos.flush();
            System.out.println("[DEBUG] Move command sent: " + direction);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to send move command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update listener to handle server responses
    private void listenForServerResponses() {
        new Thread(() -> {
            try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
                while (true) {
                    String response = dis.readUTF();
                    handleServerResponse(response);
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Lost connection to server.");
                e.printStackTrace();
                System.exit(-1);
            }
        }).start();
    }

    private void handleServerResponse(String response) {
        if (response.startsWith("GAME_STATE")) {
            // Deserialize game state
            try {
                int level = dis.readInt();
                int score = dis.readInt();
                int combo = dis.readInt();
                int moveCount = dis.readInt();
                int[] boardState = new int[SIZE * SIZE];
                for (int i = 0; i < SIZE * SIZE; i++) {
                    boardState[i] = dis.readInt();
                }
                gameEngine.updateState(level, score, combo, moveCount, boardState);
                render();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to deserialize game state: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // Handle other response types
    }

    public static void main(String[] args) {
        System.setErr(new FilteredStream(System.err));

        launch();  //** start application need call launch, finally cal  start
    }

}

class FilteredStream extends PrintStream {

    public FilteredStream(OutputStream out) {
        super(out);
    }

    @Override
    public void println(String x) {
        if (x != null && !x.contains("SLF4J: "))
            super.println(x);
    }

    @Override
    public void print(String x) {
        if (x!= null && !x.contains("WARNING: Loading FXML document with JavaFX API of version 18"))
            super.print(x);
    }
}