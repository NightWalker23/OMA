package model;

import model.exceptions.ExceptionOxygenBottom;
import model.steps.OxygenDiffusion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Model {
	private int width, height;
	private MetalCell gridMetalCell[][];
	private OxygenCell gridOxygen[][];
	private List<MetalCell> listOfAllMetalCells;
	private List<MetalCell> listOfMetalCellsI;
	private List<MetalCell> listOfMetalCellsA;
	private List<MetalCell> listOfMetalCellsAO;
	private List<OxygenCell> listOfAllOxygenCells;
	private List<OxygenCell> listOfActiveOxygenCells;


	/**
	 * First step of the algorithm.
	 *
	 * @param gridWidth     - width of array of metal cells
	 * @param gridHeight    - height of array of metal cells
	 * @param concentration - percentage of cells that will have State.A after initialization
	 */
	public void createAndInitializeGrid(int gridHeight, int gridWidth, double concentration) {
		this.width = gridWidth;
		this.height = gridHeight;

		listOfAllMetalCells = new ArrayList<>();
		listOfMetalCellsI = new ArrayList<>();
		listOfMetalCellsA = new ArrayList<>();
		listOfMetalCellsAO = new ArrayList<>();
		listOfAllOxygenCells = new ArrayList<>();
		listOfActiveOxygenCells = new ArrayList<>();

		//create a grid of oxygen cells and initialize it with empty state (false)
		gridOxygen = new OxygenCell[height][width - 1];
		for (int i = 0; i < height; i++) {
			gridOxygen[i] = new OxygenCell[width - 1];
			for (int j = 0; j < width - 1; j++) {
				gridOxygen[i][j] = new OxygenCell(i == 0, i, j);
				if (i == 0) {
					listOfActiveOxygenCells.add(gridOxygen[i][j]);
				}
			}
		}

//		gridOxygen[1][0].setActive(true);
//		gridOxygen[2][1].setActive(true);
//		gridOxygen[2][2].setActive(true);
//		gridOxygen[2][3].setActive(true);
//		listOfActiveOxygenCells.add(gridOxygen[1][0]);
//		listOfActiveOxygenCells.add(gridOxygen[2][1]);
//		listOfActiveOxygenCells.add(gridOxygen[2][2]);
//		listOfActiveOxygenCells.add(gridOxygen[2][3]);

		//add all oxygen cells from gridOxygen array to listOfAllOxygenCells by reference
		for (OxygenCell[] array : gridOxygen) {
			listOfAllOxygenCells.addAll(Arrays.asList(array));
		}


		//create a grid of metal cells and initialize it with State.I
		gridMetalCell = new MetalCell[height][width];
		for (int i = 0; i < height; i++) {
			gridMetalCell[i] = new MetalCell[width];
			for (int j = 0; j < width; j++) {
				gridMetalCell[i][j] = new MetalCell(State.I, false, i, j);
			}
		}

		//add all metal cells from gridMetalCell array to listOfAllMetalCells by reference
		for (MetalCell[] array : gridMetalCell) {
			listOfAllMetalCells.addAll(Arrays.asList(array));
		}

		//fill list of all metal cells with State.I
		listOfMetalCellsI.addAll(listOfAllMetalCells);

		//change random cells from grid to State.A according to the concentration factor
		int numberOfCellsToTransform = (int) (concentration * (this.width * this.height));

		for (int i = 0; i < numberOfCellsToTransform; i++) {
			MetalCell tmpCell;
			tmpCell = listOfMetalCellsI.get(ThreadLocalRandom.current().nextInt(0, listOfMetalCellsI.size()));
			tmpCell.setState(State.A);
			listOfMetalCellsI.remove(tmpCell);
			listOfMetalCellsA.add(tmpCell);
		}

		printGrids();
	}


	//just for test
	//remove at the end
	private void printGrids() {
//		for (int i = 0; i < height; i++) {
//			for (int j = 0; j < width; j++) {
//				System.out.print(gridMetalCell[i][j].getState() + " ");
//			}
//			System.out.println();
//		}
//		System.out.println();
//		for (int i = 0; i < height; i++) {
//			for (int j = 0; j < width; j++) {
//				System.out.print(gridMetalCell[i][j].getX() + ":" + gridMetalCell[i][j].getY() + " ");
//			}
//			System.out.println();
//		}


//		System.out.println();
//		System.out.println();
		System.out.println();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width - 1; j++) {
				System.out.print((gridOxygen[i][j].isActive() ? 1 : 0) + " ");
			}
			System.out.println();
		}
//		System.out.println();
//		for (int i = 0; i < height; i++) {
//			for (int j = 0; j < width - 1; j++) {
//				System.out.print(gridOxygen[i][j].getX() + ":" + gridOxygen[i][j].getY() + " ");
//			}
//			System.out.println();
//		}
	}


	public void startSimulation(byte minNeighboursSquare, double probabilityPT, int factorR,
								double probabilityP0, double probabilityP2, double probabilityP,
								int radiusN, int sizeGn, int iteratorS1, int iteratorS2, int steps) throws Exception {
		if (isGridInitialized()) {
			for (int j = 0; j < steps; j++) {
				for (int i = 0; i < iteratorS1; i++) {
					System.out.println("Oxygen Diffusion #" + i);
					oxygenDiffusion(gridOxygen, probabilityP0, probabilityP2, probabilityP);
				}

				transition(gridMetalCell, gridOxygen);

				for (int i = 0; i < iteratorS2; i++) {
					absorption(gridMetalCell, gridOxygen);
				}
			}
		}
	}


	private boolean isGridInitialized() {
		/*
		TODO:
		crate checking algorithm if grid was initialized
		 */

		return true;
	}


	/**
	 * Second step of the algorithm
	 *
	 * @param gridOxygen - two dimensional array of oxygen cells
	 */
	private void oxygenDiffusion(OxygenCell gridOxygen[][], double probabilityP0, double probabilityP2, double probabilityP) throws ExceptionOxygenBottom {
		if (! ((probabilityP0 > probabilityP) && (probabilityP > probabilityP2))){
			throw new ExceptionOxygenBottom("Probability P0 has to be greater than probability P and probability P has to be greater than probability P2");
		}
		OxygenDiffusion.startDiffusion(gridOxygen, listOfAllOxygenCells, listOfActiveOxygenCells, probabilityP0, probabilityP2, probabilityP, width - 1, height);

		System.out.println();
		printGrids();
	}


	/**
	 * Third step of the algorithm
	 *
	 * @param gridMetalCell - two dimensional array of metal cells
	 * @param gridOxygen    - two dimensional array of oxygen cells
	 */
	private void transition(MetalCell gridMetalCell[][], OxygenCell gridOxygen[][]) {

	}


	/**
	 * Fourth step of the algorithm
	 *
	 * @param gridMetalCell - two dimensional array of metal cells
	 * @param gridOxygen    - two dimensional array of oxygen cells
	 */
	private void absorption(MetalCell gridMetalCell[][], OxygenCell gridOxygen[][]) {

	}
}
