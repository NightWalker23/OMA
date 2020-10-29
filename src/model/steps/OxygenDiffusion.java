package model.steps;

import model.MetalCell;
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


	public static void startDiffusion(OxygenCell gridOxygen[][], MetalCell gridMetalCell[][],
									  List<OxygenCell> listOfAllOxygenCells, List<OxygenCell> listOfActiveOxygenCells,
									  double probabilityP0, double probabilityP2, double probabilityP,
									  int width, int height) throws ExceptionOxygenBottom {
		Direction direction;
		List<Direction> listOfAvailableDirections;

		if (isOxygenAtTheBottom(gridOxygen, width, height)) {
			throw new ExceptionOxygenBottom("The depth of diffusion exceeded the thickness of the sample.");
		}

		listOfActiveOxygenCells.sort(Comparator.comparing(OxygenCell::getReverseX).thenComparing(OxygenCell::getY));
		Object[] arrayOfActiveOxygenCells = listOfActiveOxygenCells.toArray();

		for (int i = 0; i < arrayOfActiveOxygenCells.length; i++) {
			listOfAvailableDirections = createListOfAvailableDirections(gridOxygen, (OxygenCell) arrayOfActiveOxygenCells[i], width, height);
			direction = settleDirectionOfDiffusion(probabilityP0, probabilityP2, probabilityP, listOfAvailableDirections,
					gridMetalCell, ((OxygenCell) arrayOfActiveOxygenCells[i]).getX(), ((OxygenCell) arrayOfActiveOxygenCells[i]).getY(), width, height);
			moveOxygen(gridOxygen, ((OxygenCell) arrayOfActiveOxygenCells[i]), listOfActiveOxygenCells, direction);
		}
	}


	private static void moveOxygen(OxygenCell[][] gridOxygen, OxygenCell el, List<OxygenCell> listOfActiveOxygenCells, Direction direction) {
		if (!direction.equals(Direction.NONE)) {

			if (el.getX() == 0 && direction.equals(Direction.DOWN)) {
				gridOxygen[el.getX() + 1][el.getY()].setActive(true);
				listOfActiveOxygenCells.add(gridOxygen[el.getX() + 1][el.getY()]);
			} else if (el.getX() != 0) {
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
	}


	private static List<Direction> createListOfAvailableDirections(OxygenCell gridOxygen[][], OxygenCell cell, int width, int height) {
		List<Direction> listOfAvailableDirections = new ArrayList<>();
		int x, y;

		x = cell.getX();
		y = cell.getY();

		if (x == 0) {
			listOfAvailableDirections.add(Direction.UP);
			listOfAvailableDirections.add(Direction.LEFT);
			listOfAvailableDirections.add(Direction.RIGHT);
			listOfAvailableDirections.add(Direction.DOWN);
		} else {
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
		}

		return listOfAvailableDirections;
	}


	private static Direction settleDirectionOfDiffusion(double probabilityP0, double probabilityP2, double probabilityP,
														List<Direction> listOfAvailableDirections, MetalCell[][] gridMetalCell,
														int x, int y, int width, int height) {
		double probability = 0.0;// = ThreadLocalRandom.current().nextDouble(0, 1);
		double mainFactor = 100.0, probabilityL, probabilityR;

		if (listOfAvailableDirections.size() > 0) {

			probabilityP /= 2.0;
			probabilityL = probabilityR = probabilityP;// + probabilityP0;
//			probabilityP2 += probabilityP + probabilityP0;

			//TODO: nie wiem jak to ma dzialac
			if (listOfAvailableDirections.contains(Direction.DOWN)) {
				if (gridMetalCell[x][y].isBorder() || gridMetalCell[x][y + 1].isBorder()) {
					probabilityP0 *= mainFactor;
				}
			}

			if (x > 0) {
				if (listOfAvailableDirections.contains(Direction.LEFT)) {
					if (gridMetalCell[x - 1][y].isBorder() || gridMetalCell[x][y].isBorder()) {
						probabilityL *= mainFactor;
					}
				}
			}

			if (y < width - 1 && x > 0) {
				if (listOfAvailableDirections.contains(Direction.RIGHT)) {
					if (gridMetalCell[x - 1][y + 1].isBorder() || gridMetalCell[x][y + 1].isBorder()) {
						probabilityR *= mainFactor;
					}
				}
			}

//			if (x > 0 && y < width - 1) {
//				if (listOfAvailableDirections.contains(Direction.UP)) {
//					if (gridMetalCell[x - 1][y].isBorder() || gridMetalCell[x - 1][y + 1].isBorder()) {
//						probabilityP2 *= mainFactor;
//					}
//				}
//			}

			double sumOfProbabilities = 0.0;
			for (Direction el : listOfAvailableDirections) {
				if (el.equals(Direction.DOWN)) sumOfProbabilities += probabilityP0;
				else if (el.equals(Direction.LEFT)) sumOfProbabilities += probabilityL;
				else if (el.equals(Direction.RIGHT)) sumOfProbabilities += probabilityR;
				else if (el.equals(Direction.UP)) sumOfProbabilities += probabilityP2;
			}

			try {
				probability = ThreadLocalRandom.current().nextDouble(0, sumOfProbabilities);
			} catch (Exception e) {
				e.printStackTrace();
			}

			double minP = 0.0, maxP = 0.0;

			for (Direction el : listOfAvailableDirections) {
				if (y == 45){
					System.out.println();
				}
				minP = maxP;
				if (el.equals(Direction.DOWN)) {
					maxP += probabilityP0;
				} else if (el.equals(Direction.LEFT)) {
					maxP += probabilityL;
				} else if (el.equals(Direction.RIGHT)) {
					maxP += probabilityR;
				} else if (el.equals(Direction.UP)) {
					maxP += probabilityP2;
				}

				if (probability >= minP && probability < maxP) {
					return el;
				}
			}
		}


//		if (listOfAvailableDirections.contains(Direction.DOWN) && probability < probabilityP0) {
//			return Direction.DOWN;
//		}
//
//		if ((listOfAvailableDirections.contains(Direction.LEFT) || listOfAvailableDirections.contains(Direction.RIGHT)) && (probability < (probabilityP0 + probabilityP))) {
//			if (listOfAvailableDirections.contains(Direction.LEFT) && listOfAvailableDirections.contains(Direction.RIGHT)) {
//				if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
//					return Direction.LEFT;
//				} else {
//					return Direction.RIGHT;
//				}
//			}
//
//			if (listOfAvailableDirections.contains(Direction.LEFT)) {
//				return Direction.LEFT;
//			}
//
//			return Direction.RIGHT;
//		}
//
//		if (listOfAvailableDirections.contains(Direction.UP)) {
//			return Direction.UP;
//		}

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
