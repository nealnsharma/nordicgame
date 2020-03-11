# Tablut
## Modeled after Viking boardgame Hnefatafl
Tablut is AI agent built to play Tablut, a variant of Hnefatafl. Tablut is played on a 9x9 checkerboard between a set of 9 white pieces and 16 black pieces. The middle square is called the throne. One of the white pieces is the king, and the others, his guards, are known as Swedes. The white side wins if the king reaches one of the edges of the board. The black pieces are known as Muscovites (referring to the Grand Duchy of Moscow). Their aim is to capture the king before he reaches the edge of the board.
The AI strategy uses the minimax algorithm with alpha-beta pruning to traverse the game state tree. The evaluation function used takes into account several variables such as both players’ current piece count, and king distance from the goal. The agent achieves a 100% win rate against the baseline agents (random and greedy) as well as simpler agents with other heuristics.

>The following are descriptions of the files:
- AI.java
  - AI player that automatically generates moves using the minimax algorithm and a heuristic.
- Board.java
  - Represents state of the Tablut game board.
- BoardWidget.java
  - A widget that displays a Tablut game.
- Controller.java
  - The input/output and GUI controller for play of Tablut.
- GUI.java
  - The GUI controller for a Tablut board and buttons.
- GUIPlayer.java
  - A Player that takes input from a GUI.
- Main.java
  - The main class to run the game and input commands.
- Move.java
  - Represents a valid move.
- NullView.java
  - A view that does nothing.
- Piece.java
  - The contents of a cell on the board.
- Player.java
  - A generic Tablut player.
- Reporter.java
  - An object that reports errors and other notifications to the user.
- Square.java
  - Represents a position on the board.
- TextPlayer.java
  -  A Player that takes input as text commands from its Controller.
- TextReporter.java
  - A Reporter that uses the standard output for messages.
- UnitTest.java
  - Suite for tests.
- Utils.java
  - Miscellaneous utitilies and error definitions.
- View.java
  - View of the Tablut board.



