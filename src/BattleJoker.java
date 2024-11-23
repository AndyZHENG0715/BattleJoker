import javafx.application.Application;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

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