## Othello Project

### Introduction

Project to implement an Othello Game Logic, GUI, and AI to play against.
Game Logic is implemented in Java with a BitBoard representation of the board (very efficient).
GUI is implemented in JavaFX.
Two (good) AI's are implemented in Java using the Minimax algorithm with Alpha-Beta pruning and Monte Carlo Tree Search.
The AI's were submitted on a submission server and were packed into a jar using CreateSubmissionJar.bash.

### Opening Book

An opening book is used, using a combination of opening books found from the web that go a few moves in depth.

### Alpha-Beta Pruning

Alpha-Beta pruning is a way to optimize the Minimax algorithm by not exploring branches that are guaranteed to be worse
than the current best move. This is done by keeping track of the best move found so far (alpha) and the best move the
opponent can make (beta). If the current move is worse than alpha, it is not explored. If the opponent's move is better
than beta, it is not explored. This is done recursively, and the algorithm returns the best move found.

This implementation of Alpha-Beta pruning uses iterative deepening, and is able to go about 7-10 moves in depth and has
advanced endgame solving.
The scoring function is not well tuned so the performance is not great.

### Monte Carlo Tree Search

Monte Carlo Tree Search with upper confidence bounds is implemented. Several playout strategies are implemented,
including random, greedy, matrix, and corner strategy with the corner strategy being the best. The corner strategy is to
play the move that maximizes the number of corners the player has.
While my Alpha-Beta pruning is able to beat my Monte Carlo Tree Search, the MCTS still performed better on the
submission server.

## Improvements

* Improve scoring function for Alpha-Beta pruning
* Improve playout strategies for Monte Carlo Tree Search (Structure similar to alphago). I want to try a neural network
  playout strategy, but the submission server does not allow external libraries (and packing them into the jar would
  make it too big for the server).
* I also want to test using a neural network to evaluate the board state for Alpha-Beta pruning.
* For these improvements, I would probably need to reimplement the game logic in Python, since it makes it easier to
  train neural networks and use external libraries.
* Improve opening book
* Remove the need for the move list ArrayList in the game logic. It is used to check pass moves, but I think it can be
  done without it. However, this would make the opening book logic more complicated.