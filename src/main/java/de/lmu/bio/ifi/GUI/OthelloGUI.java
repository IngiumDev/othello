package de.lmu.bio.ifi.GUI;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.MatrixPlayer;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.RandomPlayer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import szte.mi.Move;

import java.util.List;


public class OthelloGUI extends Application {
    private Label currentPlayerMovesMade;
    private Label playerChips;
    private Label possibleMovesLabel;
    private GridPane startlayout = new GridPane();
    private GridPane gamelayout = new GridPane();
    private OthelloGame othelloGame;
    private Button[][] othelloButtons = new Button[8][8];
    private RadioButton[] gameTypes = new RadioButton[3];
    private GameType gameType;
    private boolean isHumanPlayerOne;
    public static void main(String[] args) {
        launch(args);
    }
    /**
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Othello");

        Scene scene = new Scene(startlayout, 800, 800);
        initGame(scene);
        stage.setScene(scene);
        stage.show();


    }

    public void initGame(Scene scene) {
        // clear
        startlayout.getChildren().clear();
        ColumnConstraints colConstraints = new ColumnConstraints();
        colConstraints.setHgrow(Priority.ALWAYS);
        startlayout.getColumnConstraints().addAll(colConstraints, colConstraints, colConstraints);
        Label enterGameType = new Label("Welcome, please select game type");
        enterGameType.setFont(new Font("Arial", 25)); // Set the font size to 20

        ToggleGroup gameModeGroup = new ToggleGroup();
        gameTypes[0] = new RadioButton("Human vs Human");
        gameTypes[0].setSelected(true);
        gameTypes[0].setFont(new Font("Arial", 15)); // Set the font size to 15
        gameTypes[1] = new RadioButton("Human vs Random");
        gameTypes[1].setFont(new Font("Arial", 15)); // Set the font size to 15
        gameTypes[2] = new RadioButton("Human vs \"AI\"");
        gameTypes[2].setFont(new Font("Arial", 15)); // Set the font size to 15
        for (RadioButton gameType : gameTypes) {
            gameType.setToggleGroup(gameModeGroup);
        }
        Button startButton = new Button("Start Game");
        startButton.setFont(new Font("Arial", 15)); // Set the font size to 15

        Label disclaimer = new Label("If you select option 2 or 3, you will randomly start as player 1 or 2.");
        disclaimer.setFont(new Font("Arial", 10)); // Set the font size to 10

        startlayout.add(enterGameType, 0, 0, 3, 1); // Span 3 columns
        GridPane.setHalignment(enterGameType, HPos.CENTER); // Center horizontally
        startlayout.add(gameTypes[0], 0, 1);
        startlayout.add(gameTypes[1], 1, 1);
        startlayout.add(gameTypes[2], 2, 1);
        startlayout.add(startButton, 0, 2, 3, 1); // Span 3 columns
        GridPane.setHalignment(startButton, HPos.CENTER); // Center horizontally
        startlayout.add(disclaimer, 0, 3, 3, 1); // Span 3 columns and add it below the start button
        GridPane.setHalignment(disclaimer, HPos.CENTER);
        startlayout.setVgap(10);

        startButton.setOnAction(actionEvent -> {
            othelloGame = new OthelloGame();
            GameType gameType = GameType.values()[gameModeGroup.getToggles().indexOf(gameModeGroup.getSelectedToggle())];
            setUpGameScreen(scene, gameType);
            // get the selected game type and then get the enum value of the selected game type
            ;

        });
    }




    public void setUpGameScreen(Scene scene, GameType gameType) {
        // clear the start screen
        startlayout.getChildren().clear();
        startlayout.setVgap(0);
        StringBuilder possibleMoves = new StringBuilder();
        for (Move move : othelloGame.getPossibleMoves(true)) {
            possibleMoves.append(move.toString()).append(" ");
        }
        // Set up a game info section at the top that is as wide as the window and 50px high. It is supposed to tell which player's turn it is and how many moves have been made. Show the possible moves and how many chips each player has
        String currentPlayerMovesMadeText = "Current Player: " + othelloGame.getPlayerTurn() + ". Moves made: " + othelloGame.getMoveHistory().size()+ ".";
        currentPlayerMovesMade = new Label(currentPlayerMovesMadeText);
        currentPlayerMovesMade.setFont(new Font("Arial", 15)); // Set the font size to 15
        String possibleMovesText = "Possible moves: " + possibleMoves.toString();
        possibleMovesLabel = new Label(possibleMovesText);
        possibleMovesLabel.setFont(new Font("Arial", 15)); // Set the font size to 15
        String playerChipsText = "Player 1 has " + othelloGame.getPlayerOneChips() + " chips. Player 2 has " + othelloGame.getPlayerTwoChips() + " chips.";
        playerChips = new Label(playerChipsText);
        playerChips.setFont(new Font("Arial", 15)); // Set the font size to 15
        startlayout.add(currentPlayerMovesMade, 0, 0, 8, 1); // Span 8 columns
        startlayout.add(possibleMovesLabel, 0, 1, 8, 1); // Span 8 columns
        startlayout.add(playerChips, 0, 2, 8, 1); // Span 8 columns
        Separator separator = new Separator();
        startlayout.add(separator, 0, 3, 8, 1); // Span 8 columns and add it below the labels

        // Center the labels horizontally
        GridPane.setHalignment(currentPlayerMovesMade, HPos.CENTER);
        GridPane.setHalignment(possibleMovesLabel, HPos.CENTER);
        GridPane.setHalignment(playerChips, HPos.CENTER);

        // Set up the game board
        for (int i = 0; i < othelloButtons.length; i++) {
            for (int j = 0; j < othelloButtons[i].length; j++) {
                othelloButtons[i][j] = createGameButton(i, j, scene);
                startlayout.add(othelloButtons[i][j], i, j + 4);
                // get the current status of the button and set a white or black circle inside the button
                if (othelloGame.getBoard()[i][j] !=0) {
                Circle circle = new Circle();
                circle.radiusProperty().bind(Bindings.min(
                        scene.widthProperty().divide(8).divide(2).multiply(0.7),
                        scene.widthProperty().divide(8).divide(2).multiply(0.7)
                ));

                // Get the current status of the button and set a white or black circle inside the button
                if (othelloGame.getBoard()[i][j] == 1) {
                    circle.setFill(Color.BLACK); // white
                } else if (othelloGame.getBoard()[i][j] == 2) {
                    circle.setFill(Color.WHITE); // black
                }

                // Add the circle to the button
                othelloButtons[i][j].setGraphic(circle);}
            }
        }
        this.gameType = gameType;
        if (gameType == GameType.PLAYER_VS_RANDOM || gameType == GameType.PLAYER_VS_AI) {
            isHumanPlayerOne = Math.random() > 0;
            if (!isHumanPlayerOne) {
                // make first move for random/AI
                if (gameType == GameType.PLAYER_VS_RANDOM) {
                    Move randomMove = RandomPlayer.makeMove(othelloGame.getPlayerTurnNumber() == 1, othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1));
                    othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, randomMove.x, randomMove.y);
                } else if (gameType == GameType.PLAYER_VS_AI) {
                    Move aiMove = MatrixPlayer.makeMove(othelloGame.getPlayerTurnNumber() == 1, othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1));
                    othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, aiMove.x, aiMove.y);
                }
            }
        }
    }

    private Button createGameButton(int i, int j, Scene scene) {
        // Create a button with the text " "
        // Buttons should be alternating
        Button button = new Button();
        button.prefWidthProperty().bind(scene.widthProperty().divide(8)); // 8 is the number of columns
        button.prefHeightProperty().bind(scene.heightProperty().divide(8)); // 8 is the number of rows
        if ((i + j) % 2 == 0) {
            button.setStyle("-fx-background-color: #44633f;"); // white
        } else {
            button.setStyle("-fx-background-color: #ffa62b;"); // black
        }
        // Create action for the button
        button.setOnAction(actionEvent -> {
            if(othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, j, i)) {
                // Now if it's not players vs player, we need to make a move for the other player
                if (gameType == GameType.PLAYER_VS_RANDOM || gameType == GameType.PLAYER_VS_AI) {
                    if (gameType == GameType.PLAYER_VS_RANDOM) {
                        Move randomMove = RandomPlayer.makeMove(othelloGame.getPlayerTurnNumber() == 1, othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1));
                        othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, randomMove.x, randomMove.y);
                    } else if (gameType == GameType.PLAYER_VS_AI) {
                        Move aiMove = MatrixPlayer.makeMove(othelloGame.getPlayerTurnNumber() == 1, othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1));
                        othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, aiMove.x, aiMove.y);
                    }
                }
                // If the next player has no possible moves, we need to make a move -1, -1 move


                updateButtons(scene);
                //TODO: add game ending features
                 updateGameStatus(scene);
            }
        });
        return button;
    }
    public void updateButtons(Scene scene) {
        int[][] board = othelloGame.getBoard();
        for (int i = 0; i < othelloButtons.length; i++) {
            for (int j = 0; j < othelloButtons[i].length; j++) {
                // get the current status of the button and set a white or black circle inside the button
                if (board[i][j] !=0) {
                    Circle circle = new Circle();
                    circle.radiusProperty().bind(Bindings.min(
                            scene.widthProperty().divide(8).divide(2).multiply(0.7),
                            scene.widthProperty().divide(8).divide(2).multiply(0.7)
                    ));

                    // Get the current status of the button and set a white or black circle inside the button
                    if (board[i][j] == 1) {
                        circle.setFill(Color.BLACK); // white
                    } else if (board[i][j] == 2) {
                        circle.setFill(Color.WHITE); // black
                    }

                    // Add the circle to the button
                    othelloButtons[i][j].setGraphic(circle);}
            }
        }
    }
    public void updateGameStatus(Scene scene) {
        // Update the text of the labels
        currentPlayerMovesMade.setText("Current Player: " + othelloGame.getPlayerTurn() + ". Moves made: " + othelloGame.getMoveHistory().size()+ ".");
        playerChips.setText("Player 1 has " + othelloGame.getPlayerOneChips() + " chips. Player 2 has " + othelloGame.getPlayerTwoChips() + " chips.");
        possibleMovesLabel.setText("Possible moves: " + othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1).toString());
    }
    private void startGame(Scene scene, GameType gameType) {

        // Get which player Human is. irrelevant to human vs human
        int humanPlayer = (int) (Math.random() * 2) + 1; // Randomly choose Player 1 or Player 2 to start
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            boolean isPlayerOneTurn = othelloGame.getPlayerTurnNumber() == 1;
            List<Move> possibleMoves = othelloGame.getPossibleMoves(isPlayerOneTurn);
            if (possibleMoves!= null && possibleMoves.isEmpty()) {
                othelloGame.makeMove(!isPlayerOneTurn, 0, 0);
            }else if (possibleMoves!= null &&humanPlayer == othelloGame.getPlayerTurnNumber()) {
                if (gameType == GameType.PLAYER_VS_RANDOM) {
                    Move randomMove = RandomPlayer.makeMove(isPlayerOneTurn, possibleMoves);
                    othelloGame.makeMove(humanPlayer == 1, randomMove.x, randomMove.y);
                } else if (gameType == GameType.PLAYER_VS_AI) {
                    Move aiMove = MatrixPlayer.makeMove(isPlayerOneTurn, possibleMoves);
                    othelloGame.makeMove(humanPlayer == 1, aiMove.x, aiMove.y);
                }
            }
        }
    }

}
