package model;

public class OxygenCell {
	private boolean active;
	private int x, y;

	public OxygenCell(boolean active, int x, int y) {
		this.active = active;
		this.x = x;
		this.y = y;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getReverseX() {
		return -x;
	}

	public int getReverseY() {
		return -y;
	}
}
