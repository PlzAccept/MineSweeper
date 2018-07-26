# MineSweeper

Instruction:

1. goto Mine/ directory
2. run ant
3. play the game

There are two mode for the game: the classic mode and auto solver mode.

For classic, there are two options: 
1. run "java -jar mine.jar classic 0" for the default game
2. run "java -jar mine.jar classic 1" for guess-free game
(If you are expert of mine sweeper, you'll know that there are certain cases that you have to guess which block is mine. The guess-free mode will ensure that the game is solvable without guessing. )

Auto solve mode: (Watch the computer solve the game. Each move that computer makes can be derived from the current board. It is not cheating! )

level: 0 -> beginner, 1 -> intermediate, 2 -> expert
delay: the interval between each move.

1. run "java -jar mine.jar auto 0 (level) (delay)" for non guess-free
 e.g. "java -jar mine.jar auto 0 1 1000": computer will solve the level 1 game until it has to make a guess. And it will make a move every 1000ms.

2. run "java -jar mine.jar auto 1 (level) (delay)" for guess-free 
 e.g. "java -jar mine.jar auto 1 2 200". solve level 2 game, make a move every 200ms.
