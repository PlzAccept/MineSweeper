package edu.hpu.jxu.mine;

import java.util.*;
import java.awt.Point;
import java.io.*;

public class MineSolver {

	private final int[] di = {-1, -1, -1,  0, 0,  1, 1, 1};
	private final int[] dj = {-1,  0,  1, -1, 1, -1, 0, 1};

	private int row, col;

	private int[][] board;
	private boolean[][] flagged;
	private boolean[][] vis;

	private Queue<Point> candidate;

	/**
	 * Solve the board with initial click on (i, j)
	 */
	public MineSolver(int[][] board, int i, int j) {
		this.board = board;
		this.row = board.length;
		this.col = board[0].length;

		vis = new boolean[row][col];
		flagged = new boolean[row][col];
		candidate = new LinkedList<>();

		sweep(i, j);
	}

	// check if the board is solvable without guessing
	public boolean solvable() {
		int nummine = 0;
		for(int i = 0; i < row; i++) for(int j = 0; j < col; j++)
			if(board[i][j] == -1) nummine++;

		while(oneMove() != null);
		
		int numFlagged = 0;
		for(int i = 0; i < row; i++) for(int j = 0; j < col; j++)
			if(flagged[i][j]) numFlagged++;

		return nummine == numFlagged;
	}


	private void sweep(int i, int j) {
		if(i < 0 || i >= row || j < 0 || j >= col) return;
		if(vis[i][j]) return;


		vis[i][j] = true;

		if(board[i][j] == 0) {
			for(int k = 0; k < 8; k++)
				sweep(i + di[k], j + dj[k]);
		} else {
			candidate.add(new Point(i, j));
		}
	}

	// return one set of possible moves
	// return null if there is no such move
	public List<Integer> oneMove() {
		int size = candidate.size();


		List<Integer> movs = new ArrayList<>();
		for(int i = 0; i < size; i++) {
			Point p = candidate.poll();

			// bitmask of empty spot around the point
			int mask = 0;

			// flag count and empty spot count
			int flagCount = 0;
			int emptyCount = 0;


			for(int k = 0; k < 8; k++) {
				int ii = p.x + di[k];
				int jj = p.y + dj[k];
				if(ii < 0 || ii >= row || jj < 0 || jj >= col) continue;
				if(vis[ii][jj]) continue;

				if(flagged[ii][jj]) flagCount++;
				else {
					mask |= (1 << k);
					emptyCount++;
				}
			}

			// if no empty spot around the point 
			if(emptyCount == 0) continue;

			// if number of flag around the point is equal to the board value
			// In another word, all the empty spot around the point are safe points
			if(flagCount == board[p.x][p.y]) {

				for(int k = 0; k < 8; k++) {
					if((mask & (1 << k)) > 0) {


						sweep(p.x + di[k], p.y + dj[k]);
						movs.add(MineSolver.encode(p.x + di[k], p.y + dj[k], 1));
					}
				}

				return movs;


			// the case where all the empty spots around the point are mine
			} else if(emptyCount == board[p.x][p.y] - flagCount) {

				for(int k = 0; k < 8; k++) {
					if((mask & (1 << k)) > 0) {
						flagged[p.x + di[k]][p.y + dj[k]] = true;
						movs.add(MineSolver.encode(p.x + di[k], p.y + dj[k], 0));
					}
				}

				return movs;

			}

			int flagmask = 0xff;
			int safemask = 0xff;

			Set<Integer> sets = new HashSet<>();

			// try all possible mine location around the point
			for(int mines = 0; mines < 0x100; mines++) {

				// mask out the mines in empty spots
				int mineLoc = mines & mask;

				// check if the state has been seen
				if(sets.contains(mineLoc)) continue;
				sets.add(mineLoc);

				// if the number of mines + the number of flag is not equal to the board value
				// then this state is invalid
				if(Integer.bitCount(mineLoc) + flagCount != board[p.x][p.y]) continue;

				// try flagging the mine
				for(int k = 0; k < 8; k++) {
					if((mineLoc & (1 << k)) > 0)
						flagged[p.x + di[k]][p.y + dj[k]] = true;
				}

				// check if the flagging violates other grid
				boolean valid = true;
				
				// only need to check a 5x5 grids around point p because the flagging around p will only affect these points
				// xxxxx
				// xxxxx
				// xxpxx
				// xxxxx
				// xxxxx
				for(int ii = p.x - 2; ii <= p.x + 2; ii++) for(int jj = p.y - 2; jj <= p.y + 2; jj++) {
					if(ii < 0 || ii >= row || jj < 0 || jj >= col) continue;
					if(!vis[ii][jj]) continue;
					if(board[ii][jj] == 0) continue;

					int flg = 0;
					int emptyslots = 0;
					for(int k = 0; k < 8; k++) {
						int iii = ii + di[k];
						int jjj = jj + dj[k];
						if(iii < 0 || iii >= row || jjj < 0 || jjj >= col) continue;

						if(flagged[iii][jjj]) flg++;

						if(!vis[iii][jjj]) {
							if(!(Math.abs(iii - p.x) <= 1 && Math.abs(jjj - p.y) <= 1)) {
								if(!flagged[iii][jjj])
									emptyslots++;
							}
						}

					}

					// if the number of flag is greater than the value, then invalid
					if(flg > board[ii][jj]) valid = false;

					// if the number of empty spots + the number of flag is less than the board value, invalid
					if(emptyslots + flg < board[ii][jj]) valid = false;
				}

				// unflagging the temporary flag
				for(int k = 0; k < 8; k++) {
					if((mineLoc & (1 << k)) > 0)
						flagged[p.x + di[k]][p.y + dj[k]] = false;
				}

				// is this state is a valid mines configuration, mask it
				if(valid) {
					flagmask = flagmask & mineLoc;
					safemask = safemask & (mask & (~mineLoc));
				}

			}

			// if there is one spot that guaranteed to be safe/mine
			if(flagmask > 0 || safemask > 0) {

				// guaranteed safe points
				for(int k = 0; k < 8; k++) {
					if((safemask & (1 << k)) > 0) {
						sweep(p.x + di[k], p.y + dj[k]);
						movs.add(MineSolver.encode(p.x + di[k], p.y + dj[k], 1));
					}
				}

				// guaranteed mine points
				for(int k = 0; k < 8; k++) {
					if((flagmask & (1 << k)) > 0) {
						flagged[p.x + di[k]][p.y + dj[k]] = true;
						movs.add(MineSolver.encode(p.x + di[k], p.y + dj[k], 0));
					}
				}
				
				// add to candidate
				candidate.add(p);

				return movs;

			}

			// add point to the end of candidate queue
			candidate.add(p);

		}

		// save the board for debugging
		// try {
		// 	saveState();
		// } catch(IOException e) {}

		// if can make no moves return null
		return null;
	}

	public static int encode(int i, int j, int mode) {
		return ((mode << 16) | (i << 8) | j);
	}

	public static int[] decode(int code) {
		return new int[]{ code >> 16, (code >> 8) & 0xff, code & 0xff };
	}

	// debugging method to save the state
	private void saveState() throws IOException {
		PrintWriter out = new PrintWriter("board.txt");
		out.println(row + " " + col);

		for(int i = 0; i < row; i++) {
			String line = "";
			for(int j = 0; j < col; j++)
				line += board[i][j] + " ";
			out.println(line);
		}
		out.close();

		out = new PrintWriter("vis.txt");
		out.println(row + " " + col);

		for(int i = 0; i < row; i++) {
			String line = "";
			for(int j = 0; j < col; j++)
				line += (vis[i][j] ? 1 : 0)+ " ";
			out.println(line);
		}
		out.close();

		out = new PrintWriter("flagged.txt");
		out.println(row + " " + col);

		for(int i = 0; i < row; i++) {
			String line = "";
			for(int j = 0; j < col; j++)
				line += (flagged[i][j] ? 1 : 0) + " ";
			out.println(line);
		}
		out.close();
	}

	// debugging method to load the state
	public void loadboard() throws IOException {
		Scanner in = new Scanner(new File("board.txt"));
		row = in.nextInt();
		col = in.nextInt();

		board = new int[row][col];
		flagged = new boolean[row][col];
		vis = new boolean[row][col];

		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				board[i][j] = in.nextInt();

		in = new Scanner(new File("vis.txt"));
		in.nextInt();
		in.nextInt();
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				vis[i][j] = in.nextInt() == 1;

		in = new Scanner(new File("flagged.txt"));
		in.nextInt();
		in.nextInt();
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				flagged[i][j] = in.nextInt() == 1;
	}
}

