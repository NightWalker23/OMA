package main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import model.MetalCell;
import model.OxygenCell;
import model.State;
import model.exceptions.ExceptionGrainBorder;
import model.exceptions.ExceptionOxygenBottom;
import model.Model;
import model.exceptions.ExceptionOxygenDiffusion;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {
	public Button startButton;
	public Canvas canvas;
	public TextField fieldGridX;
	public TextField fieldGridY;
	public TextField fieldConcentration;
	public TextField fieldProbabilityP0;
	public TextField fieldProbabilityP;
	public TextField fieldProbabilityP2;
	public TextField fieldIteratorS1;
	public TextField fieldRadius;
	public TextField fieldProbabilityPT;
	public TextField fieldMinSquare;
	public TextField fieldMinSize;
	public TextField fieldIteratorS2;
	public TextField fieldIterations;
	public TextField fieldFactorR;
	public Button buttonInitializeGrid;
	public Button buttonLoadBorders;
	public CheckBox checkBoxMetalOxygen;
	GraphicsContext gc;
	Model model;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getHeight(), canvas.getWidth());

		buttonLoadBorders.setDisable(true);
		startButton.setDisable(true);

		fieldGridX.setText("10");
		fieldGridY.setText("11");
		fieldConcentration.setText("0.2");

		fieldProbabilityP0.setText("0.7");
		fieldProbabilityP.setText("0.2");
		fieldProbabilityP2.setText("0.1");
		fieldIteratorS1.setText("1");

		fieldMinSquare.setText("3");
		fieldProbabilityPT.setText("0.2");
		fieldFactorR.setText("0.1");

		fieldRadius.setText("3");
		fieldMinSize.setText("2");
		fieldIteratorS2.setText("1");

		fieldIterations.setText("1");
		checkBoxMetalOxygen.setSelected(true);
	}


	public void start(ActionEvent actionEvent) {
		double p0, p, p2, pT, factorR;
		int minNeighbourSquare, radiusN, sizeGn, iteratorS1, iteratorS2, steps;

		p0 = readDoubleFromTextField(fieldProbabilityP0);
		p = readDoubleFromTextField(fieldProbabilityP);
		p2 = readDoubleFromTextField(fieldProbabilityP2);
		iteratorS1 = readIntFromTextField(fieldIteratorS1);

		minNeighbourSquare = readIntFromTextField(fieldMinSquare);
		pT = readDoubleFromTextField(fieldProbabilityPT);
		factorR = readDoubleFromTextField(fieldFactorR);

		radiusN = readIntFromTextField(fieldRadius);
		sizeGn = readIntFromTextField(fieldMinSize);
		iteratorS2 = readIntFromTextField(fieldIteratorS2);

		steps = readIntFromTextField(fieldIterations);

		try {
			model.startSimulation(minNeighbourSquare, pT, factorR, p0, p2, p, radiusN, sizeGn, iteratorS1, iteratorS2, steps);
		} catch (ExceptionOxygenBottom | ExceptionGrainBorder | ExceptionOxygenDiffusion e) {
			showErrorMessage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (checkBoxMetalOxygen.isSelected())
			showMetalGridOnCanvas();
		else
			showOxygenGridOnCanvas();

		buttonLoadBorders.setDisable(true);
	}


	private void showMetalGridOnCanvas() {
		int cellSize = 3;

		if (model != null) {
			MetalCell holdGrid[][] = model.getGridMetalCell();
			cleanCanvas();
			for (int i = 0; i < model.getWidth(); i++) {
				for (int j = 0; j < model.getHeight(); j++) {
					MetalCell holdCell = holdGrid[j][i];
					if (holdCell.getState().equals(State.I)) {
						gc.setFill(Color.GRAY);
						gc.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
					} else if (holdCell.getState().equals(State.A)) {
						gc.setFill(Color.RED);
						gc.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
					} else if (holdCell.getState().equals(State.AO)) {
						gc.setFill(Color.DARKRED);
						gc.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
					}
				}
			}
		}
	}


	private void showOxygenGridOnCanvas() {
		int cellSize = 3;

		if (model != null) {
			OxygenCell holdGrid[][] = model.getGridOxygen();
			cleanCanvas();
			for (int i = 0; i < model.getHeight(); i++) {
				for (int j = 0; j < model.getHeight(); j++) {
					OxygenCell holdCell = holdGrid[j][i];
					if (holdCell.isActive()) {
						gc.setFill(Color.BLACK);
						gc.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
					} else {
						gc.setFill(Color.GRAY);
						gc.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
					}
				}
			}

		}
	}


	private void showErrorMessage(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText(message);
		alert.showAndWait();
	}


	private void cleanCanvas() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getHeight(), canvas.getWidth());
	}


	public void initializeGrid(ActionEvent actionEvent) {
		model = new Model();
		int height, width, minSizeGrid, maxSizeGrid;
		double concentration, minSizeConcentration, maxSizeConcentration;

		height = readIntFromTextField(fieldGridX);
		width = readIntFromTextField(fieldGridY);
		concentration = readDoubleFromTextField(fieldConcentration);
		minSizeGrid = 1;
		maxSizeGrid = 1000;
		minSizeConcentration = 0.0;
		maxSizeConcentration = 1.0;

		if (checkVarInRange(height, minSizeGrid, maxSizeGrid) && checkVarInRange(width, minSizeGrid, maxSizeGrid)) {
			if (checkVarInRange(concentration, minSizeConcentration, maxSizeConcentration)) {
				model.createAndInitializeGrid(height, width, concentration);
				buttonLoadBorders.setDisable(false);
				startButton.setDisable(false);
				cleanCanvas();

				if (checkBoxMetalOxygen.isSelected())
					showMetalGridOnCanvas();
				else
					showOxygenGridOnCanvas();
			} else {
				showErrorMessage("Concentration has to be in range <" + minSizeConcentration + "; " + maxSizeConcentration + ">");
			}
		} else {
			showErrorMessage("Height and width of the grid has to be in range <" + minSizeGrid + "; " + maxSizeGrid + ">");
		}
	}


	public void loadBorders(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Text Files", "*.txt")
		);

		boolean outcome = true;

		File selectedFile = fileChooser.showOpenDialog(null);
		String[] splittedLine;
		int counterHeight = 0, counterWidth = 0;

		if (selectedFile != null) {
			try (Scanner scanner = new Scanner(selectedFile)) {
				while (scanner.hasNext()) {
					String currentLine = scanner.nextLine().trim();
					splittedLine = currentLine.split(" ");

					for (String el : splittedLine) {
						model.getGridMetalCell()[counterHeight][counterWidth].setBorder(Integer.parseInt(el.trim()) == 1);
						counterWidth++;
					}
					counterHeight++;
					if (counterHeight != model.getHeight())
						counterWidth = 0;
				}

				if (counterHeight != model.getHeight() || counterWidth != model.getWidth()) {
					clearGrainBorders(model.getHeight(), model.getWidth(), model.getGridMetalCell());
					throw new ExceptionGrainBorder("File with grain borders do not fit with grid!");
				}
			} catch (FileNotFoundException e) {
				outcome = false;
				e.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e) {
				clearGrainBorders(model.getHeight(), model.getWidth(), model.getGridMetalCell());
				outcome = false;
				try {
					throw new ExceptionGrainBorder("File with grain borders do not fit with grid!");
				} catch (ExceptionGrainBorder exceptionGrainBorder) {
					showErrorMessage(exceptionGrainBorder.getMessage());
				}
			} catch (ExceptionGrainBorder exceptionGrainBorder) {
				outcome = false;
				showErrorMessage(exceptionGrainBorder.getMessage());
			}

			if (outcome) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Grains borders loaded successfully.");
				alert.showAndWait();
			}
		}
	}


	private void clearGrainBorders(int height, int width, MetalCell[][] gridMetalCell) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				gridMetalCell[i][j].setBorder(false);
			}
		}
	}


	private int readIntFromTextField(TextField field) {
		int value = 0;

		try {
			value = Integer.parseInt(field.getText());
		} catch (NumberFormatException ignored) {
		}

		return value;
	}


	private double readDoubleFromTextField(TextField field) {
		double value = 0;

		try {
			value = Double.parseDouble(field.getText());
		} catch (NumberFormatException ignored) {
		}

		return value;
	}


	private boolean checkVarInRange(int varToCheck, int minRange, int maxRange){
		return varToCheck >= minRange && varToCheck <= maxRange;
	}


	private boolean checkVarInRange(double varToCheck, double minRange, double maxRange){
		return varToCheck >= minRange && varToCheck <= maxRange;
	}
}
