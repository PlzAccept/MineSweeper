package edu.hpu.jxu.mine;

import java.util.Random;

// Model of the game
public class MineGame {

	public static final int IN_GAME = 0;
	public static final int WIN = 1;
	public static final int LOSS = 2;
	
	public int[][] board;
	public boolean[][] flagged, showed;

	private int row, col, mine;
	private int status;
	private int showCount;
	private int mineLeft;

	/**
	 * Constructor
	 * @param row Number of row of the board
	 * @param col Number of column of the board
	 * @param mine Number of mine in the board
	 */
	public MineGame(int row, int col, int mine) {
		this.row = row;
		this.col = col;
		this.mine = mine;

		this.status = MineGame.IN_GAME;
		this.mineLeft = mine;
	}

	/**
	 * Initialize the board upon the initial click.
	 * Make sure that the first click is not a mine.
	 * @param m The position of the initial click
	 * @param n The position of the initial click
	 */
	public void init(int m, int n) {
		
		board = new int[row][col];
		flagged = new boolean[row][col];
		showed = new boolean[row][col];


		Random rand = new Random();
		int mineCount = 0;

		// keep generating mines
		while(mineCount < mine) {
			int i = rand.nextInt(row);
			int j = rand.nextInt(col);

			if(board[i][j] != -1 && Math.abs(i - m) + Math.abs(j - n) > 2) {
				board[i][j] = -1;
				mineCount++;
			}
		}

		// count the number of mines around each grid
		for(int i = 0; i < row; i++) for(int j = 0; j < col; j++) {
			if(board[i][j] == -1) continue;

			int count = 0;
			for(int di = -1; di <= 1; di++) for(int dj = -1; dj <= 1; dj++) {
				if(di == 0 && dj == 0) continue;
				int ii = di + i;
				int jj = dj + j;
				if(ii < 0 || ii >= row || jj < 0 || jj >= col ) continue;

				if(board[ii][jj] == -1) count++;		
			}

			board[i][j] = count;
		}
	}

	/**
	 * Reveal (i, j)
	 * @param i The position to reveal
	 * @param j The position to reveal
	 */
	public void sweep(int i, int j) {

		// if (i, j) is revealed
		if(showed[i][j]) {
			sweep2(i, j);

		// if (i. j) is not revealed
		} else if(!flagged[i][j]) {
			sweep1(i, j);
		}

		// check if win
		if(showCount + mine == row * col) {
			status = MineGame.WIN;

			// auto flag if already win
			for(int k = 0; k < row; k++) for(int l = 0; l < col; l++)
				if(board[k][l] == -1)
					flagged[k][l] = true;
		}
	}

	// reveal (i, j) if (i, j) is not revealed
	private void sweep1(int i, int j) {
		if(i < 0 || i >= row || j < 0 || j >= col) return;

		if(board[i][j] == -1) {
			board[i][j] = -2;
			status = MineGame.LOSS;
			return;
		}

		if(showed[i][j]) return;

		flagged[i][j] = false;
		showed[i][j] = true;
		showCount++;

		if(board[i][j] != 0) return;

		// reveal the grid around (i, j) if (i, j) is 0
		for(int di = -1; di <= 1; di++) for(int dj = -1; dj <= 1; dj++) {
			if(di == 0 && dj == 0) continue;
			sweep1(i + di, j + dj);
		}
	}

	// reveal the grids around (i, j) when (i, j) is revealed
	private void sweep2(int i, int j) {
		if(board[i][j] == 0) return;

		int count = 0;

		for(int di = -1; di <= 1; di++) for(int dj = -1; dj <= 1; dj++) {
			int ii = i + di;
			int jj = j + dj;
			if(ii >= 0 && ii < row && jj >= 0 && jj < col && flagged[ii][jj])
				count++;
		}

		if(count != board[i][j]) return;

		for(int di = -1; di <= 1; di++) for(int dj = -1; dj <= 1; dj++) {
			int ii = i + di;
			int jj = j + dj;
			if(ii >= 0 && ii < row && jj >= 0 && jj < col) {
				if(!flagged[ii][jj])
					sweep1(ii, jj);
			}
		}
	}

	/**
	 * Flag/Unflag (i, j)
	 * @param i The position of grid to flag/unflag
	 * @param j The position of grid to flag/unflag
	 */
	public void flag(int i, int j) {
		if(showed[i][j]) return;
		
		mineLeft += flagged[i][j] ? 1 : -1;
		flagged[i][j] = !flagged[i][j];

	}

	/**
	 * Get status
	 * @return the status of the game
	 */
	public int status() {
		return this.status;
	}

	/**
	 * Get number of mines left
	 * @return the number of mines left
	 */
	public int mineLeft() {
		return this.mineLeft;
	}
}