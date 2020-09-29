package model;

public class MetalCell {
	private State state;
	private boolean border;
	private int x, y;

	public MetalCell(State state, boolean border, int x, int y) {
		this.state = state;
		this.border = border;
		this.x = x;
		this.y = y;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isBorder() {
		return border;
	}

	public void setBorder(boolean border) {
		this.border = border;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
