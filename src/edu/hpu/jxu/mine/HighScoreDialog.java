package edu.hpu.jxu.mine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HighScoreDialog extends JDialog {

	private HighScore[][] highScore;

    /**
     * Constructor
     * @param parent The parent frame
     * @param highScore The scoreboard the need to be shown
     */
	public HighScoreDialog(JFrame parent, HighScore[][] highScore) {
		super(parent);

		this.highScore = highScore;

        // tabbed pane for different level
		JTabbedPane tabbedPane = new JTabbedPane();

		String[] level = {"Beginner", "Intermediate", "Expert"};
		for(int i = 0; i < 3; i++) {
			JComponent panel = makeTextPanel(highScore[i]);
			tabbedPane.addTab(level[i], panel);
		}

		this.setLocationRelativeTo(parent);
		this.setSize(200, 275);
		this.setContentPane(tabbedPane);
		this.setVisible(true);
	}

    /**
     * Create panel for different level
     * @param highScore The scoreboard of the specific level
     */
	private JComponent makeTextPanel(HighScore[] highScore) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 2);

        label = new JLabel("#   ");
        addComponent(panel, 0, 0, 0, label, c);

        label = new JLabel("Name");
        addComponent(panel, 1, 0, 1, label, c);

        label = new JLabel("Score");
        addComponent(panel, 2, 0, 0, label, c);

        for(int i = 0; i < 10; i++) {
        	label = new JLabel((i + 1) + "");
        	addComponent(panel, 0, i + 1, 0, label, c);

        	label = new JLabel(highScore[i].getName());
        	addComponent(panel, 1, i + 1, 1, label, c);

        	label = new JLabel(highScore[i].getScore() + "", SwingConstants.CENTER);
        	addComponent(panel, 2, i + 1, 0, label, c);
        }
        return panel;
    }

    /**
     * Add component to panel using gridbaglayout
     */
    private void addComponent(JPanel panel, int x, int y, double weight, JLabel label, GridBagConstraints c) {
    	c.gridx = x;
    	c.gridy = y;
    	c.weightx = weight;
    	panel.add(label, c);
    }
}