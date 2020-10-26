package model.steps;

import model.MetalCell;
import model.OxygenCell;
import model.State;

import java.util.ArrayList;
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
			listOfIslands = createListOfIslands(listOfNeighbours, gridMetalCell);

			//TODO:
			// 1. Jak już jest lista wysep to filtracja po rozmiarze (sizeGn) i odszukanie największej wyspy spośród tych
			// 2. Znalezienie skrajnej komórki z tej wyspy - takiej żeby była najbliżej (wg układu kartezjańskiego) do naszej analizowanej komórki
			// 3. Ustalenie bezpośredniego wolnego otoczenia (takie komórki wokół mające State.I) analizowanej komórki w stylu Moora
			// 4. Ustalenie na którą wolną komórkę z otoczenia przeskoczyć -> no i przeskoczyć oczywiście
			// 5. Aktualizacja wszystkich listOf***

//			System.out.println();
//			System.out.println();
//			for (MetalCell el2 : tmpListOfNeighbours){
//				System.out.println(el2.getX() + ":" + el2.getY());
//			}
//			System.out.println();
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
			xStop = heightMC-1;
		}

		if (y + radiusN < widthMC) {
			yStop = y + radiusN;
		} else {
			yStop = widthMC-1;
		}

//		System.out.println("\nSTART =\t" + xStart + ":" + yStart);
//		System.out.println("STOP =\t" + xStop + ":" + yStop);


		for (int i = xStart; i <= xStop; i++){
			for (int j = yStart; j <= yStop; j++){
				tmpListOfNeighbours.add(gridMetalCell[i][j]);
			}
		}

		tmpListOfNeighbours.remove(gridMetalCell[x][y]);

		return tmpListOfNeighbours;
	}


	private static List<List<MetalCell>> createListOfIslands(List<MetalCell> listOfNeighbours, MetalCell[][] gridMetalCell) {
		List<List<MetalCell>> tmpListOfIslands = new ArrayList<>();

		for (MetalCell el : listOfNeighbours){
			if (el.getState().equals(State.AO)){
				//TODO:
				// 1. pobrac sasiadow komorki el typu State.AO w stylu Moore ALE tylko w granicach komorek znajdujacych sie w obszarze = na listOfNeighbours
				//		proste ify na cale otoczenie -> pobranie komorki -> czy ta komorka jest w sasiedztwie? -> jak tak to wrzuc na liste Moora
				// 2. czy ktorykolwiek z sasiadow bedacych w State.AO jest na ktorejkolwiek liscie list wysep
				//		2.0 robimy liste list na ktorych sa sasiedzi tej komorki
				//		2.1 nie ma -> tworzymy nowa liste i dodajemy tam komorke + dodajemy nowa liste na liste list
				//		2.2 jest TYLKO jedna lista gdzie sasiedzi (jeden lub wielu) juz istnieje
				//			2.2.1 dodajemy komorke el na ta liste i lecimy dalej
				//		2.3 jest wiecej niz jedna lista na ktorej znajduja sie sasiedzi
				//			2.3.1 tworzymy nowa liste
				//			2.3.2 dodajemy na ta liste wszystkie elementy list = MERGE list sasiadow
				//			2.3.3 usuwamy z listy list tamte listy
				//			2.3.4 dodajemy na ta nowa liste nasz element i lecimy dalej
				// 3. funkcje do stworzenia:
				//		3.1 pobranie sasiadow komorki w stylu moora, ale tylko tych znajdujacych sie w obszarze sasiedztwa
				//		3.2 przeszukanie listy list pod katem czy sa tam komorki z funkcji 3.1 i zwrocenie nowej listy list
				//		3.3 mergowanie list
				//		3.4 usuwanie list z listy list
			}
		}

		return tmpListOfIslands;
	}
}
