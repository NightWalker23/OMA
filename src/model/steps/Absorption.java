package model.steps;

import model.MetalCell;
import model.OxygenCell;
import model.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Absorption {

	public static void startAbsorption(MetalCell[][] gridMetalCell, OxygenCell[][] gridOxygen, int radiusN, int sizeGn, int heightMC, int widthMC,
									   List<MetalCell> listOfAllMetalCells, List<MetalCell> listOfMetalCellsA,
									   List<MetalCell> listOfMetalCellsAO, List<MetalCell> listOfMetalCellsI,
									   List<OxygenCell> listOfAllOxygenCells, List<OxygenCell> listOfActiveOxygenCells) {
		Object[] arrayOfMetalCellsA = listOfMetalCellsA.toArray();
		List<MetalCell> listOfNeighbours = new ArrayList<>();
		List<List<MetalCell>> listOfIslands = new ArrayList<>();

		for (Object el : arrayOfMetalCellsA) {
			listOfNeighbours = getNeighboursForCellA((MetalCell) el, radiusN, gridMetalCell, heightMC, widthMC);
			listOfIslands = createListOfIslands(listOfNeighbours, gridMetalCell, heightMC, widthMC);

			//TODO:
			// 1. Jak już jest lista wysep to filtracja po rozmiarze (sizeGn) i odszukanie największych wysp spośród tych
			// 2. Znalezienie nalbliższej wyspy z tych największych (może być więcej niż 1 o takim samym max rozmiarze)
			// 		znalezienie skrajnej komórki z każdej wyspy - takiej żeby była najbliżej (wg układu kartezjańskiego) do naszej analizowanej komórki
			// 3. Ustalenie bezpośredniego wolnego otoczenia (takie komórki wokół mające State.I) analizowanej komórki w stylu Moora
			// 4. Ustalenie na którą wolną komórkę z otoczenia przeskoczyć -> no i przeskoczyć oczywiście
			// 5. Aktualizacja wszystkich listOf***

			if (listOfIslands.size() > 0) {
				//1
				List<List<MetalCell>> listOfIslandsToRemove = new ArrayList<>();
				for (List<MetalCell> el2 : listOfIslands) {
					if (el2.size() < sizeGn) listOfIslandsToRemove.add(el2);
				}
				removeListOfIslands(listOfIslands, listOfIslandsToRemove);	//remove islands that size are not big enough

				listOfIslands.sort(Comparator.comparingInt(List::size));

				int maxIslandSize = listOfIslands.get(listOfIslands.size() - 1).size();
				listOfIslandsToRemove = new ArrayList<>();
				for (List<MetalCell> el2 : listOfIslands){
					if (el2.size() < maxIslandSize){
						listOfIslandsToRemove.add(el2);
					}
				}
				removeListOfIslands(listOfIslands, listOfIslandsToRemove);	//leave only these islands that size is max

				//2


				//3


				//4


				//5
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

		//TODO:
		// Tu ewentualnie można dodać przeliczenie odległości między dwoma punktami - obecnie analizowanym, a środkiem całego otoczenia
		// jeżeli odległość większa niż promień to nie dodajemy - w efekcie analizowane sąsiedztwo zamiast być kwadratem będzie kołem
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
				List<MetalCell> tmpListOfMooreNeighbour = getMooreNeighbourMetalCellInStateAOinGnNeighbour(el.getX(), el.getY(), heightMC, widthMC, gridMetalCell, listOfNeighbours);
				List<List<MetalCell>> tmpListOfIslandsContainingNeighbours = getListOfIslandsContainingNeighbours(tmpListOfMooreNeighbour, tmpListOfIslands);

				if (tmpListOfIslandsContainingNeighbours.size() == 0){
					tmpListOfIslands.add(new ArrayList<MetalCell>(){{
						add(el);
					}});
				} else if (tmpListOfIslandsContainingNeighbours.size() == 1){
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


	private static List<MetalCell> getMooreNeighbourMetalCellInStateAOinGnNeighbour(int x, int y, int height, int width,
																					MetalCell[][] gridMetalCell, List<MetalCell> listOfNeighbours) {
		List<MetalCell> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			if (gridMetalCell[x - 1][y].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x - 1][y]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y]);
		}

		//up left
		if (x > 0 && y > 0) {
			if (gridMetalCell[x - 1][y - 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x - 1][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y - 1]);
		}

		//up right
		if (x > 0 && y < width - 1) {
			if (gridMetalCell[x - 1][y + 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x - 1][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x - 1][y + 1]);
		}

		//down
		if (x < height - 1) {
			if (gridMetalCell[x + 1][y].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x + 1][y]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y]);
		}

		//down left
		if (x < height - 1 && y > 0) {
			if (gridMetalCell[x + 1][y - 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x + 1][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y - 1]);
		}

		//down right
		if (x < height - 1 && y < width - 1) {
			if (gridMetalCell[x + 1][y + 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x + 1][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x + 1][y + 1]);
		}

		//left
		if (y > 0) {
			if (gridMetalCell[x][y - 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x][y - 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y - 1]);
		}

		//right
		if (y < width - 1) {
			if (gridMetalCell[x][y + 1].getState().equals(State.AO) && listOfNeighbours.contains(gridMetalCell[x][y + 1]))
				tmpListOfMooreNeighbour.add(gridMetalCell[x][y + 1]);
		}

		return tmpListOfMooreNeighbour;
	}


	private static List<List<MetalCell>> getListOfIslandsContainingNeighbours(List<MetalCell> tmpListOfMooreNeighbour, List<List<MetalCell>> tmpListOfIslands) {
		List<List<MetalCell>> tmpListOfIslandsContainingNeighbours = new ArrayList<>();

		for (MetalCell el : tmpListOfMooreNeighbour) {
			for (List<MetalCell> list : tmpListOfIslands) {
				if (list.contains(el) && !tmpListOfIslandsContainingNeighbours.contains(list)){
					tmpListOfIslandsContainingNeighbours.add(list);
				}
			}
		}

		return tmpListOfIslandsContainingNeighbours;
	}


	private static List<MetalCell> mergeLists(List<List<MetalCell>> tmpListOfIslandsContainingNeighbours) {
		List<MetalCell> mergedList = new ArrayList<>();

		for (List<MetalCell> el1 : tmpListOfIslandsContainingNeighbours){
			for (MetalCell el2 : el1){
				if (!mergedList.contains(el2)){
					mergedList.add(el2);
				}
			}
		}

		return mergedList;
	}


	private static void removeListOfIslands(List<List<MetalCell>> tmpListOfIslands, List<List<MetalCell>> listOfListToRemoveFromListOfIslands) {
		for (List<MetalCell> el : listOfListToRemoveFromListOfIslands){
			tmpListOfIslands.remove(el);
		}
	}
}
