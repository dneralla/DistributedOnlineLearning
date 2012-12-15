package edu.illinois.ml.learning;

public class ScoreLabel {

	double Score;
	Candidate c;

	public ScoreLabel(Candidate c) {
		this.c = c;
	}

	public double getScore() {
		return Score;
	}

	public void setScore(double score) {
		Score = score;
	}

	public Candidate getC() {
		return c;
	}

	public void setC(Candidate c) {
		this.c = c;
	}

}
