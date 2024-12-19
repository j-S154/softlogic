package exampractice;

import java.util.*;
import java.sql.*;

public class TicTacToe {

    private static final int BOXSIZE = 3;
    private static char[][] box = new char[BOXSIZE][BOXSIZE];
    private static String player1Name, player2Name;
    private static int player1Wins = 0,player2Wins = 0;
    private static long startTime;
    private static Connection connection;
    static final String user="username";
	static final String pass="passord";
	static final String url="jdbc:mysql://localhost:3306/databasename";
    public static void main(String[] args) {
        try {
            setupDatabase();
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter the Name of Player 1: ");
            player1Name = scanner.nextLine();
            System.out.print("Enter the Name of Player 2: ");
            player2Name = scanner.nextLine();

            boolean playAgain;

            do {
                startNewGame();
                startTime = System.currentTimeMillis();
                boolean isPlayer1Turn = true;

                while (true) {
                    displayGrid();

                    System.out.println((isPlayer1Turn ? player1Name : player2Name) + "'s Turn (" + (isPlayer1Turn ? "X" : "O") + ")");
                    System.out.print("Enter row (0 to 2): ");
                    int row = scanner.nextInt();
                    System.out.print("Enter column (0 to 2): ");
                    int col = scanner.nextInt();

                    if (isValidMove(row,col)) {
                    	box[row][col] = isPlayer1Turn ? 'X' : 'O';
                        if (checkWinner(isPlayer1Turn ? 'X' : 'O')) {
                            displayGrid();
                            long timeTaken = (System.currentTimeMillis() - startTime)/1000;
                            System.out.println((isPlayer1Turn ? player1Name : player2Name) + " Wins!");
                            if (isPlayer1Turn) {
                                player1Wins++;
                                saveGameResult(player1Name,timeTaken,true);
                            } else {
                                player2Wins++;
                                saveGameResult(player2Name,timeTaken,true);
                            }
                            break;
                        } else if (isGridFull()) {
                            displayGrid();
                            System.out.println("It's a Draw!");
                            saveGameResult(null,(System.currentTimeMillis()-startTime)/100,false);
                            break;
                        }
                        isPlayer1Turn = !isPlayer1Turn;
                    } else {
                        System.out.println("Invalid move. Try again.");
                    }
                }
                System.out.print("Do you want to play again? (yes/no): ");
                playAgain = scanner.next().equalsIgnoreCase("yes");
            } while (playAgain);
            
            displayLeaderBoard();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    private static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(url,user,pass);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS LeaderBoard (" +
                         "name TEXT, " +
                         "wins INTEGER, " +
                         "moves INTEGER, " +
                         "time_taken INTEGER)");
        }
    }
    private static void saveGameResult(String winner, long timeTaken, boolean hasWinner) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO LeaderBoard (name,wins,moves,time_taken) VALUES(?,?,?,?)")) {
            if (hasWinner) {
                pstmt.setString(1, winner);
                pstmt.setInt(2, 1);
                pstmt.setInt(3, BOXSIZE * BOXSIZE);
                pstmt.setLong(4, timeTaken);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayLeaderBoard() {
        System.out.println("\n--- Leader Board ---");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name,SUM(wins) AS totalWins FROM LeaderBoard GROUP BY name ORDER BY totalWins DESC")) {

            while (rs.next()) {
                System.out.println(rs.getString("name") + " - Wins: " + rs.getInt("totalWins"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void closeDatabase() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void startNewGame() {
        for (int i = 0; i < BOXSIZE; i++) {
            Arrays.fill(box[i], '-');
        }
    }
    private static void displayGrid() {
        for (int i = 0; i < BOXSIZE; i++) {
            for (int j = 0; j < BOXSIZE; j++) {
                System.out.print(box[i][j] + " ");
            }
            System.out.println();
        }
    }
    private static boolean isValidMove(int row, int col) {
        return row >= 0 && row < BOXSIZE && col >= 0 && col < BOXSIZE && box[row][col] == '-';
    }

    private static boolean checkWinner(char symbol) {
        for (int i = 0; i < BOXSIZE; i++) {
            if (box[i][0] == symbol && box[i][1] == symbol && box[i][2] == symbol) return true;
            if (box[0][i] == symbol && box[1][i] == symbol && box[2][i] == symbol) return true;
        }
        return (box[0][0] == symbol && box[1][1] == symbol && box[2][2] == symbol) ||
               (box[0][2] == symbol && box[1][1] == symbol && box[2][0] == symbol);
    }
    private static boolean isGridFull() {
        for (int i = 0; i < BOXSIZE; i++) {
            for (int j = 0; j < BOXSIZE; j++) {
                if (box[i][j] == '-') return false;
            }
        }
        return true;
    }
}
