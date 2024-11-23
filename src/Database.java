import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    final static String url = "jdbc:sqlite:data/battleJoker.db";
    static Connection conn;

    public static void connect() {
        try {
            if (conn == null || conn.isClosed()) {
                // Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            // Log the error appropriately
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static void disconnect() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log the error
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                conn = null;
            }
        }
    }

    public static ArrayList<HashMap<String, String>> getScores() throws SQLException {
        String sql = "SELECT * FROM scores ORDER BY score DESC LIMIT 10";
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                HashMap<String, String> m = new HashMap<>();
                m.put("name", resultSet.getString("name"));
                m.put("score", resultSet.getString("score"));
                m.put("level", resultSet.getString("level"));
                m.put("time", resultSet.getString("time"));
                data.add(m);
            }
        }
        return data;
    }

//    public static void putScore(String name, int score, int level) throws SQLException {
//        String sql = String.format("INSERT INTO scores ('name', 'score', 'level', 'time') VALUES ('%s', %d, %d, datetime('now'))", name, score, level);
//        Statement statement = conn.createStatement();
//        statement.execute(sql);
//    }
//
//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        connect();
//        putScore("Bob", 1000, 13);
//        getScores().forEach(map->{
//            System.out.println(map.get("name"));
//        });
//    }
}