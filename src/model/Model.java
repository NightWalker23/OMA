package model;

import model.exceptions.ExceptionOxygenDiffusion;
import model.steps.Absorption;
import model.steps.OxygenDiffusion;
import model.steps.Transition;

import java.awt.*;
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

	public MetalCell[][] getGridMetalCell() {
		return gridMetalCell;
	}


	public OxygenCell[][] getGridOxygen() {
		return gridOxygen;
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}


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
	}


	public void startSimulation(int minNeighboursSquare, double probabilityPT, double factorR,
								double probabilityP0, double probabilityP2, double probabilityP,
								int radiusN, int sizeGn, int iteratorS1, int iteratorS2, int steps,
								Point point, double probabilityFactor) throws Exception {
		if (isGridInitialized()) {
			for (int j = 0; j < steps; j++) {
				for (int i = 0; i < iteratorS1; i++) {
					oxygenDiffusion(gridOxygen, probabilityP0, probabilityP2, probabilityP, probabilityFactor);
				}

				transition(gridMetalCell, gridOxygen, minNeighboursSquare, probabilityPT, factorR);

				for (int i = 0; i < iteratorS2; i++) {
					absorption(gridMetalCell, gridOxygen, radiusN, sizeGn);
				}
				point.x++;
			}
		}
	}


	/**
	 * @return true if arrays gridMetalCell and gridOxygen has been initialized, false otherwise
	 */
	private boolean isGridInitialized() {
		if (gridMetalCell != null && gridOxygen != null) {
			return true;
		}

		return false;
	}


	/**
	 * Second step of the algorithm
	 *
	 * @param gridOxygen - two dimensional array of oxygen cells
	 */
	private void oxygenDiffusion(OxygenCell gridOxygen[][], double probabilityP0,
								 double probabilityP2, double probabilityP, double probabilityFactor) throws Exception {
		double threshold = 0.0001;

//		if (!((probabilityP0 > probabilityP) && (probabilityP > probabilityP2))) {
//			throw new ExceptionOxygenDiffusion("Probability P0 has to be greater than probability P and probability P has to be greater than probability P2");
//		}

		if (Math.abs((probabilityP0 + probabilityP + probabilityP2) - 1.0) > threshold) {
			throw new ExceptionOxygenDiffusion("Sum of probabilities p0, p and p2 has to be equal 1.0!");
		}

		OxygenDiffusion.startDiffusion(gridOxygen, gridMetalCell, listOfActiveOxygenCells,
				probabilityP0, probabilityP2, probabilityP, width - 1, height, probabilityFactor);
	}


	/**
	 * Third step of the algorithm
	 *
	 * @param gridMetalCell       - two dimensional array of metal cells
	 * @param gridOxygen          - two dimensional array of oxygen cells
	 * @param minNeighboursSquare - minimum number of cells in Moore neighbourhood required for cell in State.AO to change into State.A in some special conditions
	 * @param probabilityPT       - probability for cell in State.AO to change into State.A in some special conditions
	 * @param factorR             - factor used to calculate probability for cell in State.AO to change into State.A and to transmit oxygen in some special conditions
	 */
	private void transition(MetalCell gridMetalCell[][], OxygenCell gridOxygen[][],
							int minNeighboursSquare, double probabilityPT, double factorR) {
		Transition.startTransition(gridMetalCell, gridOxygen, minNeighboursSquare, probabilityPT, factorR,
				listOfAllMetalCells, listOfMetalCellsI, listOfMetalCellsA, listOfMetalCellsAO,
				listOfAllOxygenCells, listOfActiveOxygenCells, height, width);
	}


	/**
	 * Fourth step of the algorithm
	 *
	 * @param gridMetalCell - two dimensional array of metal cells
	 * @param gridOxygen    - two dimensional array of oxygen cells
	 * @param radiusN		- size of the radius of the area that will be searched for each cell to look for cells is State.AO
	 * @param sizeGn		-
	 */
	private void absorption(MetalCell gridMetalCell[][], OxygenCell gridOxygen[][], int radiusN, int sizeGn) {
		Absorption.startAbsorption(gridMetalCell, radiusN, sizeGn, height, width, listOfMetalCellsA, listOfMetalCellsI);
	}
}
