# Tablut
## Modeled after Viking boardgame Hnefatafl
Tablut is AI agent built to play Tablut, a variant of Hnefatafl. Tablut is played on a 9x9 checkerboard between a set of 9 white pieces and 16 black pieces. The middle square is called the throne. One of the white pieces is the king, and the others, his guards, are known as Swedes. The white side wins if the king reaches one of the edges of the board. The black pieces are known as Muscovites (referring to the Grand Duchy of Moscow). Their aim is to capture the king before he reaches the edge of the board.
The AI strategy uses the minimax algorithm with alpha-beta pruning to traverse the game state tree. The evaluation function used takes into account several variables such as both players’ current piece count, and king distance from the goal. The agent achieves a 100% win rate against the baseline agents (random and greedy) as well as simpler agents with other heuristics.

>The following are descriptions of the files:
- 
-



