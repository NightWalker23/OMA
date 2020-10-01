package model.steps;

import model.MetalCell;
import model.OxygenCell;
import model.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Transition {

	public static void startTransition(MetalCell[][] gridMetalCell, OxygenCell[][] gridOxygen, int minNeighboursSquare,
									   double probabilityPT, int factorR,
									   List<MetalCell> listOfAllMetalCells, List<MetalCell> listOfMetalCellsI,
									   List<MetalCell> listOfMetalCellsA, List<MetalCell> listOfMetalCellsAO,
									   List<OxygenCell> listOfAllOxygenCells, List<OxygenCell> listOfActiveOxygenCells,
									   int height, int width) {
		List<MetalCell> tmpListOfAllMetalCells, tmpListOfMetalCellsI, tmpListOfMetalCellsA, tmpListOfMetalCellsAO;
		MetalCell currentMetalCell;

		tmpListOfAllMetalCells = new ArrayList<>();
		tmpListOfMetalCellsI = new ArrayList<>();
		tmpListOfMetalCellsA = new ArrayList<>();
		tmpListOfMetalCellsAO = new ArrayList<>();

		copyFromListToTmpLists(listOfAllMetalCells, tmpListOfAllMetalCells, tmpListOfMetalCellsI, tmpListOfMetalCellsA, tmpListOfMetalCellsAO);

		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				currentMetalCell = gridMetalCell[i][j];
				if (currentMetalCell.getState().equals(State.A)) {
					transitionVariantB(i, j, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, listOfActiveOxygenCells, gridOxygen);
				} else if (currentMetalCell.getState().equals(State.AO)) {

				}
			}
		}

		copyFromTmpListToListsAndGrid(listOfAllMetalCells, listOfMetalCellsI, listOfMetalCellsA, listOfMetalCellsAO,
				tmpListOfAllMetalCells, tmpListOfMetalCellsI, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, gridMetalCell);
	}

	private static void copyFromListToTmpLists(List<MetalCell> listOfAllMetalCells,
											   List<MetalCell> tmpListOfAllMetalCells, List<MetalCell> tmpListOfMetalCellsI,
											   List<MetalCell> tmpListOfMetalCellsA, List<MetalCell> tmpListOfMetalCellsAO) {
		MetalCell tmpMetalCell;

		for (MetalCell el : listOfAllMetalCells) {
			try {
				tmpMetalCell = (MetalCell) el.clone();
				tmpListOfAllMetalCells.add(tmpMetalCell);

				if (tmpMetalCell.getState().equals(State.I)) {
					tmpListOfMetalCellsI.add(tmpMetalCell);
				} else if (tmpMetalCell.getState().equals(State.A)) {
					tmpListOfMetalCellsA.add(tmpMetalCell);
				} else if (tmpMetalCell.getState().equals(State.AO)) {
					tmpListOfMetalCellsAO.add(tmpMetalCell);
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}


	private static void copyFromTmpListToListsAndGrid(List<MetalCell> listOfAllMetalCells, List<MetalCell> listOfMetalCellsI,
													  List<MetalCell> listOfMetalCellsA, List<MetalCell> listOfMetalCellsAO,
													  List<MetalCell> tmpListOfAllMetalCells, List<MetalCell> tmpListOfMetalCellsI,
													  List<MetalCell> tmpListOfMetalCellsA, List<MetalCell> tmpListOfMetalCellsAO,
													  MetalCell[][] gridMetalCell) {
		listOfAllMetalCells.clear();
		listOfAllMetalCells.addAll(tmpListOfAllMetalCells);
		listOfMetalCellsI.clear();
		listOfMetalCellsI.addAll(tmpListOfMetalCellsI);
		listOfMetalCellsA.clear();
		listOfMetalCellsA.addAll(tmpListOfMetalCellsA);
		listOfMetalCellsAO.clear();
		listOfMetalCellsAO.addAll(tmpListOfMetalCellsAO);

		for (MetalCell el : listOfAllMetalCells) {
			gridMetalCell[el.getX()][el.getY()] = el;
		}
	}


	private static List<OxygenCell> createListOfNeighbourOxygen(int x, int y, OxygenCell[][] gridOxygen) {
		List<OxygenCell> tmpListOfOxygenCells = new ArrayList<>();

		//left up
		if (y > 0) {
			tmpListOfOxygenCells.add(gridOxygen[x][y - 1]);
		}

		//left down
		if (y > 0 && x < gridOxygen.length - 1) {
			tmpListOfOxygenCells.add(gridOxygen[x + 1][y - 1]);
		}

		//right up
		if (y < gridOxygen[0].length - 1) {
			tmpListOfOxygenCells.add(gridOxygen[x][y]);
		}

		//right down
		if (x < gridOxygen.length - 1 && y < gridOxygen[0].length - 1) {
			tmpListOfOxygenCells.add(gridOxygen[x + 1][y]);
		}

		return tmpListOfOxygenCells;
	}


	private static List<OxygenCell> createListOfNeighbourOxygenActive(int x, int y, OxygenCell[][] gridOxygen) {
		List<OxygenCell> tmpListOfOxygenCells = new ArrayList<>();

		//left up
		if (y > 0) {
			if (gridOxygen[x][y - 1].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x][y - 1]);
			}
		}

		//left down
		if (y > 0 && x < gridOxygen.length - 1) {
			if (gridOxygen[x + 1][y - 1].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x + 1][y - 1]);
			}
		}

		//right up
		if (y < gridOxygen[0].length - 1) {
			if (gridOxygen[x][y].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x][y]);
			}
		}

		//right down
		if (x < gridOxygen.length - 1 && y < gridOxygen[0].length - 1) {
			if (gridOxygen[x + 1][y].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x + 1][y]);
			}
		}

		return tmpListOfOxygenCells;
	}


	private static List<OxygenCell> createListOfNeighbourOxygenInactive(int x, int y, OxygenCell[][] gridOxygen) {
		List<OxygenCell> tmpListOfOxygenCells = new ArrayList<>();

		//left up
		if (y > 0) {
			if (!gridOxygen[x][y - 1].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x][y - 1]);
			}
		}

		//left down
		if (y > 0 && x < gridOxygen.length - 1) {
			if (!gridOxygen[x + 1][y - 1].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x + 1][y - 1]);
			}
		}

		//right up
		if (y < gridOxygen[0].length - 1) {
			if (!gridOxygen[x][y].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x][y]);
			}
		}

		//right down
		if (x < gridOxygen.length - 1 && y < gridOxygen[0].length - 1) {
			if (!gridOxygen[x + 1][y].isActive()) {
				tmpListOfOxygenCells.add(gridOxygen[x + 1][y]);
			}
		}

		return tmpListOfOxygenCells;
	}


	private static void transitionVariantB(int x, int y, List<MetalCell> tmpListOfMetalCellsA, List<MetalCell> tmpListOfMetalCellsAO,
										   List<OxygenCell> listOfActiveOxygenCells, OxygenCell[][] gridOxygen) {
		List<OxygenCell> listOfNeighbourOxygen;
		OxygenCell randomOxygenCell;
		MetalCell tmpMetalCell = null;

		for (MetalCell el : tmpListOfMetalCellsA) {
			if (el.getX() == x && el.getY() == y) {
				tmpMetalCell = el;
				break;
			}
		}

		listOfNeighbourOxygen = createListOfNeighbourOxygenActive(tmpMetalCell.getX(), tmpMetalCell.getY(), gridOxygen);

		if (listOfNeighbourOxygen.size() > 0) {
			tmpMetalCell.setState(State.AO);
			tmpListOfMetalCellsA.remove(tmpMetalCell);
			tmpListOfMetalCellsAO.add(tmpMetalCell);
			randomOxygenCell = listOfNeighbourOxygen.get(ThreadLocalRandom.current().nextInt(0, listOfNeighbourOxygen.size()));

			if (randomOxygenCell.getX() != 0) {
				randomOxygenCell.setActive(false);
				listOfActiveOxygenCells.remove(randomOxygenCell);
			}
		}
	}

//	private static void transitionVariantB(List<MetalCell> listOfMetalCellsA, List<MetalCell> listOfMetalCellsAO,
//										   List<OxygenCell> listOfActiveOxygenCells, OxygenCell[][] gridOxygen,
//										   List<MetalCell> tmpListOfMetalCellsA, List<MetalCell> tmpListOfMetalCellsAO) {
//		listOfMetalCellsA.sort(Comparator.comparing(MetalCell::getReverseX).thenComparing(MetalCell::getY));
//		Object[] arrayOfActiveMetalCells = listOfMetalCellsA.toArray();
//
//		List<OxygenCell> listOfNeighbourOxygen;
//		OxygenCell randomOxygenCell;
//
//		for (int i = 0; i < arrayOfActiveMetalCells.length; i++) {
//			listOfNeighbourOxygen = createListOfNeighbourOxygenActive(((MetalCell) arrayOfActiveMetalCells[i]).getX(), ((MetalCell) arrayOfActiveMetalCells[i]).getY(), gridOxygen);
//
//			if (listOfNeighbourOxygen.size() > 0) {
//				((MetalCell) arrayOfActiveMetalCells[i]).setState(State.AO);
//				listOfMetalCellsA.remove((MetalCell) arrayOfActiveMetalCells[i]);
//				listOfMetalCellsAO.add((MetalCell) arrayOfActiveMetalCells[i]);
//				randomOxygenCell = listOfNeighbourOxygen.get(ThreadLocalRandom.current().nextInt(0, listOfNeighbourOxygen.size()));
//
//				if (randomOxygenCell.getX() != 0) {
//					randomOxygenCell.setActive(false);
//					listOfActiveOxygenCells.remove(randomOxygenCell);
//				}
//			}
//		}
//	}
}
