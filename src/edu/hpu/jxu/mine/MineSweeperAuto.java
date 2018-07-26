package edu.hpu.jxu.mine;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class MineSweeperAuto extends JFrame {

	private final int GRID_LENGTH = 30;
	private final String dataFile = "highscore";

	private int ROW;
	private int COL;
	private int LEVEL;

	private boolean guessFree;
	private int interval;

	private Timer timer;
	private boolean inGame;
	private MineGame game;
	private ImageIcon emptyImg, flagImg, bombImg, bombdeath, bombmisflagged;
	private ImageIcon[] numImg;
	private JLabel timerLabel, mineLabel;

	private Queue<Integer> moves;

	private MineSolver solver;

	private JLabel[][] gameButtons;

	public MineSweeperAuto(int level, boolean guessFree, int interval) throws Exception {
		loadImage();

		this.guessFree = guessFree;
		this.interval = interval;

		this.setTitle("Mine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		LEVEL = level;
		init();

		this.setVisible(true);
	}

	private void init() {
		timer = new Timer(interval, new TimerActionListener());

		ROW = Config.ROW[LEVEL];
		COL = Config.COL[LEVEL];

		game = new MineGame(ROW, COL, Config.MINE[LEVEL]);

		if(guessFree) {
			do {
				game.init(ROW/2, COL/2);
				solver = new MineSolver(game.board, ROW/2, COL/2);
			} while(!solver.solvable());

			solver = new MineSolver(game.board, ROW/2, COL/2);
		} else {
			game.init(ROW/2, COL/2);
			solver = new MineSolver(game.board, ROW/2, COL/2);
		}

		moves = new LinkedList<>();
		moves.add(MineSolver.encode(ROW/2, COL/2, 1));

		List<Integer> m;
		while((m = solver.oneMove()) != null) {
			moves.addAll(m);
		}


		inGame = true;

		this.getContentPane().removeAll();

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridLayout(ROW, COL));
		mainPanel.setPreferredSize(new Dimension(COL * GRID_LENGTH, ROW * GRID_LENGTH));

		gameButtons = new JLabel[ROW][COL];
		for(int i = 0; i < ROW; i++) {
			for(int j = 0; j < COL; j++) {
				JLabel button = new JLabel();
				button.setIcon(emptyImg);
				mainPanel.add(button);

				gameButtons[i][j] = button;
			}
		}

		JPanel infoPanel = new JPanel();

		JButton start = new JButton("Start");

		timerLabel = new JLabel("0");
        timerLabel.setPreferredSize(new Dimension(60, 40));
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timerLabel.setBorder(BorderFactory.createTitledBorder("Time"));
        
        mineLabel = new JLabel(game.mineLeft() + "");
        mineLabel.setPreferredSize(new Dimension(60, 40));
        mineLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        mineLabel.setBorder(BorderFactory.createTitledBorder("Mine"));

		infoPanel.add(mineLabel);
		infoPanel.add(start);
		infoPanel.add(timerLabel);

		this.getContentPane().add(infoPanel, BorderLayout.NORTH);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.pack();

		timer.start();
	}

	private void loadImage() {
		emptyImg = new ImageIcon("Images/blank.gif");
		Image image = emptyImg.getImage();
		emptyImg = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));

		flagImg = new ImageIcon("Images/bombflagged.gif");
		image = flagImg.getImage();
		flagImg = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));
	
		bombImg = new ImageIcon("Images/bombrevealed.gif");
		image = bombImg.getImage();
		bombImg = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));

		bombdeath = new ImageIcon("Images/bombdeath.gif");
		image = bombdeath.getImage();
		bombdeath = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));

		bombmisflagged = new ImageIcon("Images/bombmisflagged.gif");
		image = bombmisflagged.getImage();
		bombmisflagged = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));
		
		numImg = new ImageIcon[9];
		for(int i = 0; i < 9; i++) {
			numImg[i] = new ImageIcon("Images/open" + i + ".gif");
			image = numImg[i].getImage();
			numImg[i] = new ImageIcon(image.getScaledInstance(GRID_LENGTH, GRID_LENGTH, Image.SCALE_SMOOTH));
		}
	}



	private void update() throws Exception {

		// update the frame
		for(int i = 0; i < ROW; i++) for(int j = 0; j < COL; j++) {
			if(game.showed[i][j]) {
				gameButtons[i][j].setIcon(numImg[game.board[i][j]]);
			} else if(game.flagged[i][j]) {
				gameButtons[i][j].setIcon(flagImg);
			} else {
				gameButtons[i][j].setIcon(emptyImg);
			}
		}

		if(game.status() == MineGame.LOSS) {

			for(int i = 0; i < ROW; i++) for(int j = 0; j < COL; j++) {

				// reveal bomb
				if(game.board[i][j] < 0 && !game.flagged[i][j]) {

					if(game.board[i][j] == -1) // other bombs
						gameButtons[i][j].setIcon(bombImg);
					else  // the bomb which causes the death
						gameButtons[i][j].setIcon(bombdeath);

				// misflagged
				} else if(game.flagged[i][j] && game.board[i][j] >= 0) {
					gameButtons[i][j].setIcon(bombmisflagged);
				}
			}

			inGame = false;
			timer.stop();

		} else if(game.status() == MineGame.WIN) {

			mineLabel.setText("0");

			inGame = false;
			timer.stop();


			
		}
	}

	private class TimerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!moves.isEmpty()) {
				int[] mov = MineSolver.decode(moves.poll());

				//System.out.println(mov[0] + " " + mov[1] + " " + mov[2]);
				

				if(mov[0] == 1) {
					game.sweep(mov[1], mov[2]);
				} else {
					game.flag(mov[1], mov[2]);
				}

				try { update(); } catch(Exception ex) {}
			}
		}
	}

}