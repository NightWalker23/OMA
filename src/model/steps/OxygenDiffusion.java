package model.steps;

import model.OxygenCell;
import model.exceptions.ExceptionOxygenBottom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class OxygenDiffusion {
	private enum Direction {
		DOWN, LEFT, RIGHT, UP, NONE;
	}


	public static void startDiffusion(OxygenCell gridOxygen[][], List<OxygenCell> listOfAllOxygenCells,
									  List<OxygenCell> listOfActiveOxygenCells, double probabilityP0,
									  double probabilityP2, double probabilityP, int width, int height) throws ExceptionOxygenBottom {
		Direction direction;
		List<Direction> listOfAvailableDirections;

		if (isOxygenAtTheBottom(gridOxygen, width, height)) {
			throw new ExceptionOxygenBottom("The depth of diffusion exceeded the thickness of the sample.");
		}

		listOfActiveOxygenCells.sort(Comparator.comparing(OxygenCell::getReverseX).thenComparing(OxygenCell::getY));
		Object[] arrayOfActiveOxygenCells = listOfActiveOxygenCells.toArray();

		for (int i = 0; i < arrayOfActiveOxygenCells.length; i++) {
			listOfAvailableDirections = createListOfAvailableDirections(gridOxygen, (OxygenCell) arrayOfActiveOxygenCells[i], width, height);
			direction = settleDirectionOfDiffusion(probabilityP0, probabilityP2, probabilityP, listOfAvailableDirections);
			moveOxygen(gridOxygen, ((OxygenCell) arrayOfActiveOxygenCells[i]), listOfActiveOxygenCells, direction);
		}
	}


	private static void moveOxygen(OxygenCell[][] gridOxygen, OxygenCell el, List<OxygenCell> listOfActiveOxygenCells, Direction direction) {
		if (!direction.equals(Direction.NONE)) {
			switch (direction) {
				case UP:
					gridOxygen[el.getX() - 1][el.getY()].setActive(true);
					el.setActive(false);
					listOfActiveOxygenCells.remove(el);
					listOfActiveOxygenCells.add(gridOxygen[el.getX() - 1][el.getY()]);
					break;

				case LEFT:
					gridOxygen[el.getX()][el.getY() - 1].setActive(true);
					el.setActive(false);
					listOfActiveOxygenCells.remove(el);
					listOfActiveOxygenCells.add(gridOxygen[el.getX()][el.getY() - 1]);
					break;

				case RIGHT:
					gridOxygen[el.getX()][el.getY() + 1].setActive(true);
					el.setActive(false);
					listOfActiveOxygenCells.remove(el);
					listOfActiveOxygenCells.add(gridOxygen[el.getX()][el.getY() + 1]);
					break;

				case DOWN:
					gridOxygen[el.getX() + 1][el.getY()].setActive(true);
					if (el.getX() != 0) {
						el.setActive(false);
						listOfActiveOxygenCells.remove(el);
					}
					listOfActiveOxygenCells.add(gridOxygen[el.getX() + 1][el.getY()]);
					break;
			}
		}
	}


	private static List<Direction> createListOfAvailableDirections(OxygenCell gridOxygen[][], OxygenCell cell, int width, int height) {
		List<Direction> listOfAvailableDirections = new ArrayList<>();
		int x, y;

		x = cell.getX();
		y = cell.getY();

		if (x > 0) {
			if (!gridOxygen[x - 1][y].isActive()) listOfAvailableDirections.add(Direction.UP);
		}

		if (x < height - 1) {
			if (!gridOxygen[x + 1][y].isActive()) listOfAvailableDirections.add(Direction.DOWN);
		}

		if (y > 0) {
			if (!gridOxygen[x][y - 1].isActive()) listOfAvailableDirections.add(Direction.LEFT);
		}

		if (y < width - 1) {
			if (!gridOxygen[x][y + 1].isActive()) listOfAvailableDirections.add(Direction.RIGHT);
		}

		return listOfAvailableDirections;
	}


	private static Direction settleDirectionOfDiffusion(double probabilityP0, double probabilityP2, double probabilityP,
														List<Direction> listOfAvailableDirections) {
		double probability = ThreadLocalRandom.current().nextDouble(0, 1);

		if (listOfAvailableDirections.contains(Direction.DOWN) && probability < probabilityP0) {
			return Direction.DOWN;
		}

		if ((listOfAvailableDirections.contains(Direction.LEFT) || listOfAvailableDirections.contains(Direction.RIGHT)) && (probability < (probabilityP0 + probabilityP))) {
			if (listOfAvailableDirections.contains(Direction.LEFT) && listOfAvailableDirections.contains(Direction.RIGHT)) {
				if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
					return Direction.LEFT;
				} else {
					return Direction.RIGHT;
				}
			}

			if (listOfAvailableDirections.contains(Direction.LEFT)) {
				return Direction.LEFT;
			}

			return Direction.RIGHT;
		}

		if (listOfAvailableDirections.contains(Direction.UP)) {
			return Direction.UP;
		}

		//niezależnie od wylosowanego prawdopodobieństwa - jak się nic nie trafiło to wybierz jakikolwiek wolny kierunek
//		if (	listOfAvailableDirections.contains(Direction.DOWN) ||
//				listOfAvailableDirections.contains(Direction.LEFT) ||
//				listOfAvailableDirections.contains(Direction.RIGHT)) {
//			return (listOfAvailableDirections.get(ThreadLocalRandom.current().nextInt(0, listOfAvailableDirections.size())));
//		}

		return Direction.NONE;
	}


	/**
	 * @param gridOxygen - two dimensional array of oxygen cells
	 * @param width      - width of the two dimensional array gridOxygen
	 * @param height     - height of the two dimensional array gridOxygen
	 * @return true if at least one of the cells at the bottom of the gridOxygen array is filled with oxygen, otherwise return false
	 */
	private static boolean isOxygenAtTheBottom(OxygenCell gridOxygen[][], int width, int height) {
		for (int i = 0; i < width; i++) {
			if (gridOxygen[height - 1][i].isActive()) {
				return true;
			}
		}

		return false;
	}
}
