package edu.hpu.jxu.mine;

public class HighScore {
	private String name;
	private int score;

	public HighScore(String name, int score) {
		this.name = name;
		this.score = score;
	}

	public String getName() {
		return this.name;
	}

	public int getScore() {
		return this.score;
	}
}