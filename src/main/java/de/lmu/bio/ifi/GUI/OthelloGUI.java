package de.lmu.bio.ifi.GUI;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.PlayerMove;
import de.lmu.bio.ifi.Players.HumanPlayer;
import de.lmu.bio.ifi.Players.MatrixPlayer;
import de.lmu.bio.ifi.Players.RandomPlayer;
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
import szte.mi.Player;

import java.util.ArrayList;
import java.util.List;


public class OthelloGUI extends Application {
    private Label currentPlayerMovesMade;
    private Label playerChips;
    private Label possibleMovesLabel;
    private GridPane startlayout = new GridPane();
    private GridPane gamelayout = new GridPane();
    private OthelloGame othelloGame;
    private Button[][] othelloButtons = new Button[8][8];
    private RadioButton[] playerOnePlayerTypes = new RadioButton[3];
    private RadioButton[] playerTwoPlayerTypes = new RadioButton[3];
    private GameType gameType;
    private Player playerOne;
    private Player playerTwo;
    private boolean isPlayerOneHuman;
    private boolean isPlayerTwoHuman;
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
        Label enterGameType = new Label("Welcome, please select player type for player 1 and 2");
        enterGameType.setFont(new Font("Arial", 25)); // Set the font size to 20

        ToggleGroup playerOneSelectGroupe = new ToggleGroup();
        playerOnePlayerTypes[0] = new RadioButton("Human");
        playerOnePlayerTypes[0].setSelected(true);
        playerOnePlayerTypes[0].setFont(new Font("Arial", 15)); // Set the font size to 15
        playerOnePlayerTypes[1] = new RadioButton("Random");
        playerOnePlayerTypes[1].setFont(new Font("Arial", 15)); // Set the font size to 15
        playerOnePlayerTypes[2] = new RadioButton("\"AI\"");
        playerOnePlayerTypes[2].setFont(new Font("Arial", 15)); // Set the font size to 15
        for (RadioButton playerType : playerOnePlayerTypes) {
            playerType.setToggleGroup(playerOneSelectGroupe);
        }
        ToggleGroup playerTwoSelectGroupe = new ToggleGroup();
        playerTwoPlayerTypes[0] = new RadioButton("Human");
        playerTwoPlayerTypes[0].setSelected(true);
        playerTwoPlayerTypes[0].setFont(new Font("Arial", 15)); // Set the font size to 15
        playerTwoPlayerTypes[1] = new RadioButton("Random");
        playerTwoPlayerTypes[1].setFont(new Font("Arial", 15)); // Set the font size to 15
        playerTwoPlayerTypes[2] = new RadioButton("\"AI\"");
        playerTwoPlayerTypes[2].setFont(new Font("Arial", 15)); // Set the font size to 15
        for (RadioButton playerType : playerTwoPlayerTypes) {
            playerType.setToggleGroup(playerTwoSelectGroupe);
        }
        Button startButton = new Button("Start Game");
        startButton.setFont(new Font("Arial", 15)); // Set the font size to 15
        Label disclaimer = new Label("First row is Player One, second row is Player Two");
        disclaimer.setFont(new Font("Arial", 15)); // Set the font size to 15
        startlayout.add(enterGameType, 0, 0, 3, 1); // Span 3 columns
        GridPane.setHalignment(enterGameType, HPos.CENTER); // Center horizontally
        startlayout.add(playerOnePlayerTypes[0], 0, 1);
        startlayout.add(playerOnePlayerTypes[1], 1, 1);
        startlayout.add(playerOnePlayerTypes[2], 2, 1);
        startlayout.add(playerTwoPlayerTypes[0], 0, 2);
        startlayout.add(playerTwoPlayerTypes[1], 1, 2);
        startlayout.add(playerTwoPlayerTypes[2], 2, 2);

        startlayout.add(startButton, 0, 3, 3, 1); // Span 3 columns
        GridPane.setHalignment(startButton, HPos.CENTER); // Center horizontally
        startlayout.add(disclaimer, 0, 4, 3, 1); // Span 3 columns and add it below the start button
        GridPane.setHalignment(disclaimer, HPos.CENTER);
        startlayout.setVgap(10);

        startButton.setOnAction(actionEvent -> {
            othelloGame = new OthelloGame();
            // Initialize players
            isPlayerOneHuman = playerOnePlayerTypes[0].isSelected();
            isPlayerTwoHuman = playerTwoPlayerTypes[0].isSelected();
            // Player one
            if (playerOnePlayerTypes[0].isSelected()) {
                playerOne = new HumanPlayer();
            } else if (playerOnePlayerTypes[1].isSelected()) {
                playerOne = new RandomPlayer();
            } else if (playerOnePlayerTypes[2].isSelected()) {
                playerOne = new MatrixPlayer();
            }
            // Player two
            if (playerTwoPlayerTypes[0].isSelected()) {
                playerTwo = new HumanPlayer();
            } else if (playerTwoPlayerTypes[1].isSelected()) {
                playerTwo = new RandomPlayer();
            } else if (playerTwoPlayerTypes[2].isSelected()) {
                playerTwo = new MatrixPlayer();
            }
            playerOne.init(0, 0, null);
            playerTwo.init(1, 0, null);
            setUpGameScreen(scene, gameType);
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
        // If the first player is not human, do first move
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
        if (!isPlayerOneHuman) {
            makeFirstMove(scene);
        }
        if (!(playerOne instanceof HumanPlayer) && !(playerTwo instanceof HumanPlayer)) {
            doAIGame(scene);
        }
        System.out.println("test");

    }

    private void doAIGame(Scene scene) {
        // Disable all buttons
        setButtonsStatus(false);
        // Play game
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            // Check who's turn it is
            boolean isPlayerOne = othelloGame.getPlayerTurnNumber() == 1;
            // get move history
            ArrayList<PlayerMove> moveHistory = othelloGame.getMoveHistory();
            Move nextMove;
            if (isPlayerOne) {
                 nextMove = playerOne.nextMove(moveHistory.get(moveHistory.size()-1), 0, 0);
            } else {
                 nextMove = playerTwo.nextMove(moveHistory.get(moveHistory.size()-1), 0, 0);
            }
            // Make the move
            othelloGame.makeMove(isPlayerOne, nextMove.x, nextMove.y);
            updateButtons(scene);
            //updateGameStatus(scene);

        }
        endGame(scene);
    }

    private void makeFirstMove(Scene scene) {
        Move move = playerOne.nextMove(null, 0, 0);
        othelloGame.makeMove(true, move.x, move.y);
        updateButtons(scene);
        updateGameStatus(scene);
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
            makePlayerMove(i, j, scene);
        });
        return button;
    }

    private void makePlayerMove(int i, int j, Scene scene) {
        setButtonsStatus(true);
        Move move = new Move(i,j);
        boolean isPlayerOne = othelloGame.getPlayerTurnNumber() == 1;
        List<Move> moves = othelloGame.getPossibleMoves(isPlayerOne);
        if (moves == null || moves.isEmpty()) {
            setButtonsStatus(false);
            return;
        }
        boolean wasMoveValid = moves.stream().anyMatch(m -> m.x == i && m.y == j);
        if (wasMoveValid) {
            othelloGame.makeMove(isPlayerOne, i, j);
            updateGame(scene);
        } else {
            setButtonsStatus(false);
        }
    }
    private void setButtonsStatus(boolean disable) {
        for (Button[] row : othelloButtons) {
            for (Button button : row) {
                button.setDisable(disable);
            }
        }
    }
    private void updateGame(Scene scene) {
        updateButtons(scene);
        if (othelloGame.gameStatus() != GameStatus.RUNNING) {
            endGame(scene);
            return;
        }
        if (!(othelloGame.getPlayerTurnNumber() == 1 ? playerOne instanceof HumanPlayer : playerTwo instanceof HumanPlayer)) {
            doAIMove(scene, null);
        }
        updateButtons(scene);
        if (othelloGame.gameStatus() != GameStatus.RUNNING) {
            endGame(scene);
            return;
        }
        updateGameStatus(scene);
        System.out.println(othelloGame);
        setButtonsStatus(false);
    }
    public void doAIMove(Scene scene, Move move) {
        if (othelloGame.gameStatus() != GameStatus.RUNNING) {
            updateGame(scene);
            return;
        }
        Player currentPlayer = othelloGame.getPlayerTurnNumber() == 1 ? playerOne : playerTwo;
        boolean isPlayerHuman = currentPlayer instanceof HumanPlayer;
        if (!isPlayerHuman) {
            Move nextMove = currentPlayer.nextMove(move, 0, 0);
            othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, nextMove == null ? -1 : nextMove.x, nextMove == null ? -1 : nextMove.y);
            if (othelloGame.gameStatus() != GameStatus.RUNNING) {
                updateGame(scene);
                return;
            }
            if (othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1).isEmpty()) {
                othelloGame.makeMove(othelloGame.getPlayerTurnNumber() == 1, -1, -1);
                doAIMove(scene, null);
            }
        }
    }

    public void updateButtons(Scene scene) {
        for (int i = 0; i < othelloButtons.length; i++) {
            for (int j = 0; j < othelloButtons[i].length; j++) {
                int cell = othelloGame.getCell(i, j);
                if (cell != 0) {
                    Circle circle = new Circle();
                    circle.radiusProperty().bind(Bindings.min(
                            scene.widthProperty().divide(8).divide(2).multiply(0.7),
                            scene.widthProperty().divide(8).divide(2).multiply(0.7)
                    ));

                    if (cell == 1) {
                        circle.setFill(Color.BLACK); // white
                    } else if (cell == 2) {
                        circle.setFill(Color.WHITE); // black
                    }

                    othelloButtons[i][j].setGraphic(circle);
                }
            }
        }
    }

    public void updateGameStatus(Scene scene) {
        // Update the text of the labels
        currentPlayerMovesMade.setText("Current Player: " + othelloGame.getPlayerTurn() + ". Moves made: " + othelloGame.getMoveHistory().size()+ ".");
        playerChips.setText("Player 1 has " + othelloGame.getPlayerOneChips() + " chips. Player 2 has " + othelloGame.getPlayerTwoChips() + " chips.");
        possibleMovesLabel.setText("Possible moves: " + othelloGame.getPossibleMoves(othelloGame.getPlayerTurnNumber() == 1).toString());
    }

    public void endGame(Scene scene) {
        //TODO
        // Disable all buttons
        setButtonsStatus(false);
        // Show the game result
        Label gameResult = new Label();
        gameResult.setFont(new Font("Arial", 15)); // Set the font size to 15
        switch (othelloGame.gameStatus()) {
            case DRAW:
                gameResult.setText("It's a draw!");
                break;
            case PLAYER_1_WON:
                gameResult.setText("Player 1 won!");
                break;
            case PLAYER_2_WON:
                gameResult.setText("Player 2 won!");
                break;
        }
        startlayout.add(gameResult, 0, 9, 8, 1); // Span 8 columns and add it below the labels
        GridPane.setHalignment(gameResult, HPos.CENTER);
        // Add a button to start a new game
        Button newGameButton = new Button("New Game");
        newGameButton.setFont(new Font("Arial", 15)); // Set the font size to 15
        startlayout.add(newGameButton, 0, 10, 8, 1); // Span 8 columns and add it below the labels
        GridPane.setHalignment(newGameButton, HPos.CENTER);
        newGameButton.setOnAction(actionEvent -> {
            initGame(scene);
        });
    }

}
