package model;

public class Info {
	public int steps;
	public double percentageA, percentageAO, percentageI, A_AO;

	public Info(int steps, double percentageA, double percentageAO, double percentageI, double A_AO) {
		this.steps = steps;
		this.percentageA = percentageA;
		this.percentageAO = percentageAO;
		this.percentageI = percentageI;
		this.A_AO = A_AO;
	}
}
