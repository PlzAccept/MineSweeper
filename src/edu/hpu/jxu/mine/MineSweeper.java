package edu.hpu.jxu.mine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class MineSweeper extends JFrame {

	private final int GRID_LENGTH = 30;
	private final String dataFile = "highscore";

	private boolean guessFree;
	private int ROW;
	private int COL;
	private int LEVEL;
	private HighScore[][] highScore;

	private boolean firstClick;
	private int time;
	private Timer timer;
	private boolean inGame;
	private MineGame game;
	private ImageIcon emptyImg, flagImg, bombImg, bombdeath, bombmisflagged;
	private ImageIcon[] numImg;
	private JLabel timerLabel, mineLabel;

	private JLabel[][] gameButtons;

	/**
	 * Constructor
	 */
	public MineSweeper(boolean guessFree) throws Exception {
		this.guessFree = guessFree;

		loadImage();
		loadHighScore();

		this.setTitle("Mine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		// initialize menu
		initMenu();

		// initialize timer for timing the game
		timer = new Timer(1000, new TimerActionListener());
		init();

		this.setVisible(true);
	}

	/**
     * Initialize the main frame
	 */
	private void init() {

		// reset timer
		timer.stop();
		time = 0;

		// reset first click
		firstClick = true;

		// set #row and #col base on level
		ROW = Config.ROW[LEVEL];
		COL = Config.COL[LEVEL];

		// create a new game
		game = new MineGame(ROW, COL, Config.MINE[LEVEL]);
		inGame = true;

		// reset the content pane
		this.getContentPane().removeAll();

		// main game panel
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridLayout(ROW, COL));
		mainPanel.setPreferredSize(new Dimension(COL * GRID_LENGTH, ROW * GRID_LENGTH));

		gameButtons = new JLabel[ROW][COL];
		for(int i = 0; i < ROW; i++) {
			for(int j = 0; j < COL; j++) {
				JLabel button = new JLabel();
				button.setIcon(emptyImg);
				mainPanel.add(button);

				ClickEvent event = new ClickEvent(i, j);
				button.addMouseListener(event);

				gameButtons[i][j] = button;
			}
		}


		// information panel
		JPanel infoPanel = new JPanel();

		JButton start = new JButton("Start");
		start.addActionListener(new StartButtonListener());

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

		// add all to content pane
		this.getContentPane().add(infoPanel, BorderLayout.NORTH);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.pack();
	}

	/**
	 * Initialize menu
	 */
	private void initMenu() {
		String[] levels = {"Beginner", "Intermediate", "Expert"};

		JMenuItem menuItem;
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Mines");

		// show high score button
		menuItem = new JMenuItem("High score");
		menuItem.addActionListener(new HighScoreButtonListener());
		menu.add(menuItem);


		// level selector
		JMenu levelMenu = new JMenu("Level");

		for(int i = 0; i < 3; i++) {
			menuItem = new JMenuItem(levels[i]);
			menuItem.addActionListener(new LevelSelector(i));
			levelMenu.add(menuItem);
		}

		menu.add(levelMenu);
		menuBar.add(menu);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Load icons into memory
	 */
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

	/**
	 * Load scoreboard into memory
	 */
	private void loadHighScore() throws Exception {
		highScore = new HighScore[3][10];
		
		File scores = new File(dataFile);

		// if socreboard file exist
		if(scores.exists()) {

			FileSystem sys = FileSystems.getDefault();
			Path path = sys.getPath(dataFile);

			FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);

			ByteBuffer buffer = ByteBuffer.allocate(2048);
			buffer.order(ByteOrder.BIG_ENDIAN);

			buffer.rewind();
			int nread = channel.read(buffer);

			if(nread == -1) {
				System.out.println("error");
				System.exit(0);
			}

			buffer.rewind();
			int i = 0;
			while(buffer.position() < nread) {
				int nbytes = buffer.getInt();
				byte[] bytes = new byte[nbytes];
				buffer.get(bytes, 0, nbytes);
				String name = new String(bytes);
				int score = buffer.getInt();

				highScore[i/10][i%10] = new HighScore(name, score);
				i++;
			}

		// if scoreboard file does not exist, load default scoreboard and create new file
		} else {
			for(int i = 0; i < 3; i++) for(int j = 0; j < 10; j++) {
				highScore[i][j] = new HighScore("xxx", 99999);
			}

			saveData();
		}
	}

	/**
	 * Save new scoreboard into disk
	 * Format: NameLen1 Name1 Score1 NameLen2 Name2 Score2 etc... in byte stream 
	 */
	private void saveData() throws Exception {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(dataFile));
		for(int i = 0; i < 3; i++) for(int j = 0; j < 10; j++) {
			out.writeInt(highScore[i][j].getName().getBytes().length);
			out.writeBytes(highScore[i][j].getName());
			out.writeInt(highScore[i][j].getScore());
		}

		out.close();
	}

	/**
	 * Update the main frame
	 */
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

		// if loss
		if(game.status() == MineGame.LOSS) {

			for(int i = 0; i < ROW; i++) for(int j = 0; j < COL; j++) {

				// reveal bomb
				if(game.board[i][j] < 0 && !game.flagged[i][j]) {

					if(game.board[i][j] == -1) // other bombs
						gameButtons[i][j].setIcon(bombImg);
					else  // the bomb which causes the death
						gameButtons[i][j].setIcon(bombdeath);

				// show misflagged
				} else if(game.flagged[i][j] && game.board[i][j] >= 0) {
					gameButtons[i][j].setIcon(bombmisflagged);
				}
			}

			inGame = false;
			timer.stop();

		// if win
		} else if(game.status() == MineGame.WIN) {

			mineLabel.setText("0");

			inGame = false;
			timer.stop();


			// new high score
			if(time < highScore[LEVEL][9].getScore()) {
				String name = JOptionPane.showInputDialog(this, "New high Score!\nYour name is: ", "high score!", JOptionPane.QUESTION_MESSAGE);
				if(name != null) {

					// record the new high score
					int i = 9;
					while(i > 0 && time < highScore[LEVEL][i - 1].getScore()) {
						highScore[LEVEL][i] = highScore[LEVEL][i - 1];
						i--;
					}

					if(name.length() > 10)
						name = name.substring(0, 10);
					highScore[LEVEL][i] = new HighScore(name, time);

					// save new scoreboard into disk
					saveData();
				}

			// not new high score
			} else {
				JOptionPane.showMessageDialog(this, "Congradulations! You Win!");
			}
		}
	}

	// show high score dialog
	private class HighScoreButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			HighScoreDialog dialog = new HighScoreDialog(MineSweeper.this, highScore);
		}
	}

	// select level
	private class LevelSelector implements ActionListener {
		private int level;

		public LevelSelector(int l) {
			level = l;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LEVEL = level;
			init();
		}
	}

	// increment timer every second
	private class TimerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			time++;
			timerLabel.setText(time + "");
		}
	}

	// start a new game
	private class StartButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			init();
		}
	}

	private class ClickEvent implements MouseListener {
		private boolean leftdown, rightdown;
		private int i, j;

		/**
		 * Constructor
		 * @param i The grid is in i-th row
		 * @param j The grid is in j-th row
		 */
		public ClickEvent(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
        public void mousePressed(MouseEvent e) {}

		@Override
        public void mouseReleased(MouseEvent e) {
        	// check if it's in game 
        	if(!inGame) return;

        	// generate the board on first click
			if(firstClick) {

				MineSolver solver = null;

				if(guessFree) {
					// make sure the board is guess-free
					// keeping generating the board until it is solvable via only logic
					// may lag if keep getting unsolvable board
					do {
						game.init(i, j);
						solver = new MineSolver(game.board, i, j);
					} while(!solver.solvable());

				} else {
					game.init(i, j);
				}

				// start the timer
				timer.start();

				firstClick = false;
			}

			// left click
			if(e.getButton() == MouseEvent.BUTTON1) {
				game.sweep(i, j);

			// right click
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				game.flag(i, j);
				mineLabel.setText(game.mineLeft() + "");
			}

			// update frame
			try {
				update();
			} catch(Exception ex) {}
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
	}


	public static void main(String[] args) throws Exception {

		if(args[0].equals("classic")) {

			if(args[1].equals("0")) {
				new MineSweeper(false);
			} else if(args[1].equals("1")) {
				new MineSweeper(true);
			}

		} else if(args[0].equals("auto")) {

			if(args[1].equals("0")) {
				new MineSweeperAuto(Integer.parseInt(args[2]), false, Integer.parseInt(args[3]));
			} else if(args[1].equals("1")) {
				new MineSweeperAuto(Integer.parseInt(args[2]), true, Integer.parseInt(args[3]));
			}

		}

		//new MineSweeper(false);
		//new MineSweeperAuto(2, false, 200);

	}
}