package model.steps;

import model.MetalCell;
import model.OxygenCell;
import model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Transition {

	public static void startTransition(MetalCell[][] gridMetalCell, OxygenCell[][] gridOxygen, int minNeighboursSquare,
									   double probabilityPT, double factorR,
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
					transitionStateA(i, j, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, listOfActiveOxygenCells,
							gridOxygen);
				} else if (currentMetalCell.getState().equals(State.AO)) {
					transitionStateAO(i, j, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, listOfAllOxygenCells,
							listOfActiveOxygenCells, gridOxygen, gridMetalCell, minNeighboursSquare, probabilityPT,
							factorR, height, width);
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


	private static void transitionStateA(int x, int y, List<MetalCell> tmpListOfMetalCellsA, List<MetalCell> tmpListOfMetalCellsAO,
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


	private static void transitionStateAO(int x, int y, List<MetalCell> tmpListOfMetalCellsA,
										  List<MetalCell> tmpListOfMetalCellsAO, List<OxygenCell> listOfAllOxygenCells,
										  List<OxygenCell> listOfActiveOxygenCells, OxygenCell[][] gridOxygen, MetalCell[][] gridMetalCell,
										  int minNeighboursSquare, double probabilityPT, double factorR, int height, int width) {
		int numberOfNeighbourInStateAO = getNumberOfVonNeumannNeighbourInStateAO(x, y, height, width, gridMetalCell);

		//variant C -> stable state, nothing to do
		if (numberOfNeighbourInStateAO != 4) {

			//variant D -> check if cell can form a square with other cells in Moore neighbour if number of neighbours in State.AO is >= than minNeighboursSquare
			//do operations with some probability probabilityPT
			if (getNumberMooreNeighbourMetalCellInStateAO(x, y, height, width, gridMetalCell) >= minNeighboursSquare &&
					(isLeftUpSquare(x, y, height, width, gridMetalCell) ||
							isLeftDownSquare(x, y, height, width, gridMetalCell) ||
							isRightUpSquare(x, y, height, width, gridMetalCell) ||
							isRightDownSquare(x, y, height, width, gridMetalCell)) &&
					ThreadLocalRandom.current().nextDouble(0, 1) < probabilityPT) {
				transitCellFromStateAOToStateAAndReleaseOxygen(x, y, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, listOfActiveOxygenCells, gridOxygen);
			} else if (ThreadLocalRandom.current().nextDouble(0, 1) < Math.pow(probabilityPT, (1.0 / factorR))) { //variant E -> every other case
				transitCellFromStateAOToStateAAndReleaseOxygen(x, y, tmpListOfMetalCellsA, tmpListOfMetalCellsAO, listOfActiveOxygenCells, gridOxygen);
			}
		}
	}


	private static int getNumberOfVonNeumannNeighbourInStateAO(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		int numberOfNeighbourInStateAO = 0;

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(State.AO)) numberOfNeighbourInStateAO++;
		}

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(State.AO)) numberOfNeighbourInStateAO++;
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(State.AO)) numberOfNeighbourInStateAO++;
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(State.AO)) numberOfNeighbourInStateAO++;
		}

		return numberOfNeighbourInStateAO;
	}


	private static int getNumberMooreNeighbourMetalCellInStateAO(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y]);
		}

		//up left
		if (x > 0 && y > 0) {
			if (gridMetalCell[x - 1][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y - 1]);
		}

		//up right
		if (x > 0 && y < width - 1) {
			if (gridMetalCell[x - 1][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y + 1]);
		}

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y]);
		}

		//down left
		if (x < height - 1 && y > 0) {
			if (gridMetalCell[x + 1][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y - 1]);
		}

		//down right
		if (x < height - 1 && y < width - 1) {
			if (gridMetalCell[x + 1][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y + 1]);
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y - 1]);
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y + 1]);
		}

		return tmpListOfMooreNeighbour.size();
	}


	private static boolean isLeftUpSquare(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y]);
		}

		//up left
		if (x > 0 && y > 0) {
			if (gridMetalCell[x - 1][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y - 1]);
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y - 1]);
		}

		return tmpListOfMooreNeighbour.size() == 3;
	}


	private static boolean isLeftDownSquare(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y]);
		}

		//down left
		if (x < height - 1 && y > 0) {
			if (gridMetalCell[x + 1][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y - 1]);
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y - 1]);
		}

		return tmpListOfMooreNeighbour.size() == 3;
	}


	private static boolean isRightUpSquare(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y]);
		}

		//up right
		if (x > 0 && y < width - 1) {
			if (gridMetalCell[x - 1][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y + 1]);
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y + 1]);
		}

		return tmpListOfMooreNeighbour.size() == 3;
	}


	private static boolean isRightDownSquare(int x, int y, int height, int width, MetalCell[][] gridMetalCell) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y]);
		}

		//down right
		if (x < height - 1 && y < width - 1) {
			if (gridMetalCell[x + 1][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y + 1]);
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(State.AO))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y + 1]);
		}

		return tmpListOfMooreNeighbour.size() == 3;
	}


	private static void transitCellFromStateAOToStateAAndReleaseOxygen(int x, int y, List<MetalCell> tmpListOfMetalCellsA,
																	   List<MetalCell> tmpListOfMetalCellsAO,
																	   List<OxygenCell> listOfActiveOxygenCells,
																	   OxygenCell[][] gridOxygen) {
		List<OxygenCell> listOfNeighbourOxygenInactive = createListOfNeighbourOxygenInactive(x, y, gridOxygen);

		if (listOfNeighbourOxygenInactive.size() > 0) {
			MetalCell tmpMetalCell = null;
			OxygenCell tmpOxygenCell = listOfNeighbourOxygenInactive.get(ThreadLocalRandom.current().nextInt(0, listOfNeighbourOxygenInactive.size()));

			for (MetalCell el : tmpListOfMetalCellsAO) {
				if (el.getX() == x && el.getY() == y) {
					tmpMetalCell = el;
					break;
				}
			}

			tmpMetalCell.setState(State.A);
			tmpListOfMetalCellsAO.remove(tmpMetalCell);
			tmpListOfMetalCellsA.add(tmpMetalCell);
			tmpOxygenCell.setActive(true);
			listOfActiveOxygenCells.add(tmpOxygenCell);
		}
	}

}
