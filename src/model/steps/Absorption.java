package model.steps;

import model.MetalCell;
import model.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Absorption {

	public static void startAbsorption(MetalCell[][] gridMetalCell, int radiusN, int sizeGn, int heightMC, int widthMC,
									   List<MetalCell> listOfMetalCellsA, List<MetalCell> listOfMetalCellsI) {
		Object[] arrayOfMetalCellsA = listOfMetalCellsA.toArray();
		List<MetalCell> listOfNeighbours = new ArrayList<>();
		List<List<MetalCell>> listOfIslands = new ArrayList<>();

		for (Object el : arrayOfMetalCellsA) {
			listOfNeighbours = getNeighboursForCellA((MetalCell) el, radiusN, gridMetalCell, heightMC, widthMC);
			listOfIslands = createListOfIslands(listOfNeighbours, gridMetalCell, heightMC, widthMC);

			if (listOfIslands.size() > 0) {
				List<List<MetalCell>> listOfIslandsToRemove = new ArrayList<>();
				for (List<MetalCell> el2 : listOfIslands) {
					if (el2.size() < sizeGn) listOfIslandsToRemove.add(el2);
				}
				removeListOfIslands(listOfIslands, listOfIslandsToRemove);    //remove islands that size are not big enough

				listOfIslands.sort(Comparator.comparingInt(List::size));

				if (listOfIslands.size() > 0) {
					int maxIslandSize = listOfIslands.get(listOfIslands.size() - 1).size();
					listOfIslandsToRemove = new ArrayList<>();
					for (List<MetalCell> el2 : listOfIslands) {
						if (el2.size() < maxIslandSize) {
							listOfIslandsToRemove.add(el2);
						}
					}
					removeListOfIslands(listOfIslands, listOfIslandsToRemove);    //leave only these islands that size is max

					MetalCell closestIslandCell = getClosestCellFromIslands(listOfIslands, el);

					List<MetalCell> neighbourInStateI = getMooreNeighbourMetalCellInDesiredStateInGnNeighbour(((MetalCell) el).getX(), ((MetalCell) el).getY(),
							heightMC, widthMC, gridMetalCell, listOfNeighbours, State.I);

					if (neighbourInStateI.size() > 0) {
						MetalCell cellToSwitchTo = findCellToSwitchTo(el, neighbourInStateI, closestIslandCell);

						if (cellToSwitchTo != null) {
							cellToSwitchTo.setState(State.A);
							((MetalCell) el).setState(State.I);
							listOfMetalCellsA.add(cellToSwitchTo);
							listOfMetalCellsI.remove(cellToSwitchTo);
							listOfMetalCellsA.remove(el);
							listOfMetalCellsI.add((MetalCell) el);
						}
					}
				}
			}
		}
	}


	private static List<MetalCell> getNeighboursForCellA(MetalCell el, int radiusN, MetalCell[][] gridMetalCell, int heightMC, int widthMC) {
		List<MetalCell> tmpListOfNeighbours = new ArrayList<>();

		int x = el.getX();
		int y = el.getY();
		int xStart, xStop, yStart, yStop;

		if (x - radiusN >= 0) {
			xStart = x - radiusN;
		} else {
			xStart = 0;
		}

		if (y - radiusN >= 0) {
			yStart = y - radiusN;
		} else {
			yStart = 0;
		}

		if (x + radiusN < heightMC) {
			xStop = x + radiusN;
		} else {
			xStop = heightMC - 1;
		}

		if (y + radiusN < widthMC) {
			yStop = y + radiusN;
		} else {
			yStop = widthMC - 1;
		}

		for (int i = xStart; i <= xStop; i++) {
			for (int j = yStart; j <= yStop; j++) {
				tmpListOfNeighbours.add(gridMetalCell[i][j]);
			}
		}

		tmpListOfNeighbours.remove(gridMetalCell[x][y]);

		return tmpListOfNeighbours;
	}


	private static List<List<MetalCell>> createListOfIslands(List<MetalCell> listOfNeighbours, MetalCell[][] gridMetalCell,
															 int heightMC, int widthMC) {
		List<List<MetalCell>> tmpListOfIslands = new ArrayList<>();

		for (MetalCell el : listOfNeighbours) {
			if (el.getState().equals(State.AO)) {
				List<MetalCell> tmpListOfMooreNeighbour = getMooreNeighbourMetalCellInDesiredStateInGnNeighbour(el.getX(), el.getY(), heightMC, widthMC, gridMetalCell, listOfNeighbours, State.AO);
				List<List<MetalCell>> tmpListOfIslandsContainingNeighbours = getListOfIslandsContainingNeighbours(tmpListOfMooreNeighbour, tmpListOfIslands);

				if (tmpListOfIslandsContainingNeighbours.size() == 0) {
					tmpListOfIslands.add(new ArrayList<MetalCell>() {{
						add(el);
					}});
				} else if (tmpListOfIslandsContainingNeighbours.size() == 1) {
					tmpListOfIslandsContainingNeighbours.get(0).add(el);
				} else {
					List<MetalCell> mergedList = mergeLists(tmpListOfIslandsContainingNeighbours);
					removeListOfIslands(tmpListOfIslands, tmpListOfIslandsContainingNeighbours);
					mergedList.add(el);
					tmpListOfIslands.add(mergedList);
				}
			}
		}

		return tmpListOfIslands;
	}


	private static List<MetalCell> getMooreNeighbourMetalCellInDesiredStateInGnNeighbour(int x, int y, int height, int width,
																						 MetalCell[][] gridMetalCell, List<MetalCell> listOfNeighbours, State state) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x - 1][y]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y]);
		}

		//up left
		if (x > 0 && y > 0) {
			if (gridMetalCell[x - 1][y - 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x - 1][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y - 1]);
		}

		//up right
		if (x > 0 && y < width - 1) {
			if (gridMetalCell[x - 1][y + 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x - 1][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y + 1]);
		}

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x + 1][y]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y]);
		}

		//down left
		if (x < height - 1 && y > 0) {
			if (gridMetalCell[x + 1][y - 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x + 1][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y - 1]);
		}

		//down right
		if (x < height - 1 && y < width - 1) {
			if (gridMetalCell[x + 1][y + 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x + 1][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y + 1]);
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y - 1]);
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(state) && listOfNeighbours.contains(gridMetalCell[x][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y + 1]);
		}

		return tmpListOfMooreNeighbour;
	}


	private static List<List<MetalCell>> getListOfIslandsContainingNeighbours(List<MetalCell> tmpListOfMooreNeighbour, List<List<MetalCell>> tmpListOfIslands) {
		List<List<MetalCell>> tmpListOfIslandsContainingNeighbours = new ArrayList<>();

		for (MetalCell el : tmpListOfMooreNeighbour) {
			for (List<MetalCell> list : tmpListOfIslands) {
				if (list.contains(el) && !tmpListOfIslandsContainingNeighbours.contains(list)) {
					tmpListOfIslandsContainingNeighbours.add(list);
				}
			}
		}

		return tmpListOfIslandsContainingNeighbours;
	}


	private static List<MetalCell> mergeLists(List<List<MetalCell>> tmpListOfIslandsContainingNeighbours) {
		List<MetalCell> mergedList = new ArrayList<>();

		for (List<MetalCell> el1 : tmpListOfIslandsContainingNeighbours) {
			for (MetalCell el2 : el1) {
				if (!mergedList.contains(el2)) {
					mergedList.add(el2);
				}
			}
		}

		return mergedList;
	}


	private static void removeListOfIslands(List<List<MetalCell>> tmpListOfIslands, List<List<MetalCell>> listOfListToRemoveFromListOfIslands) {
		for (List<MetalCell> el : listOfListToRemoveFromListOfIslands) {
			tmpListOfIslands.remove(el);
		}
	}


	private static MetalCell getClosestCellFromIslands(List<List<MetalCell>> listOfIslands, Object el) {
		MetalCell tmpClosestCell = null;
		double distance = 0.0;

		tmpClosestCell = listOfIslands.get(0).get(0);
		distance = getDistanceBetweenCells((MetalCell) el, listOfIslands.get(0).get(0));

		for (List<MetalCell> list : listOfIslands) {
			for (MetalCell mc : list) {
				if (getDistanceBetweenCells((MetalCell) el, mc) < distance) {
					distance = getDistanceBetweenCells((MetalCell) el, mc);
					tmpClosestCell = mc;
				}
			}
		}

		return tmpClosestCell;
	}


	private static double getDistanceBetweenCells(MetalCell cell1, MetalCell cell2) {
		return Math.sqrt(Math.pow(cell2.getX() - cell1.getX(), 2) + Math.pow(cell2.getY() - cell1.getY(), 2));
	}


	private static MetalCell findCellToSwitchTo(Object el, List<MetalCell> neighbourInStateI, MetalCell closestIslandCell) {
		MetalCell cellToSwitchTo = neighbourInStateI.get(0);
		double distance = getDistanceBetweenCells(neighbourInStateI.get(0), closestIslandCell);


		for (MetalCell mc : neighbourInStateI) {
			if (getDistanceBetweenCells(mc, closestIslandCell) < distance &&
					getDistanceBetweenCells(mc, closestIslandCell) < getDistanceBetweenCells((MetalCell) el, closestIslandCell)) {
				cellToSwitchTo = mc;
				distance = getDistanceBetweenCells(mc, closestIslandCell);
			}
		}

		if (getDistanceBetweenCells(cellToSwitchTo, closestIslandCell) > getDistanceBetweenCells((MetalCell) el, closestIslandCell)) {
			cellToSwitchTo = null;
		}

		return cellToSwitchTo;
	}
}
