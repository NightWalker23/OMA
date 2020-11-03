package main;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.MetalCell;
import model.OxygenCell;
import model.State;
import model.exceptions.ExceptionGrainBorder;
import model.exceptions.ExceptionOxygenBottom;
import model.Model;
import model.exceptions.ExceptionOxygenDiffusion;
import model.exceptions.ExceptionWithMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
	public Button buttonVisualize;
	public RadioButton radioMetal;
	public RadioButton radioOxygen;
	public RadioButton radioBorders;
	public TextField fieldBorderDiffusion;
	public Text textIterations;
	public Text textDepth;
	public Button buttonSaveImage;
	public Button buttonLoadBordersBMP;
	GraphicsContext gc;
	Model model;
	int cellSize, depth;
	Point counterOfSteps;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cellSize = 1;
		depth = 0;
		counterOfSteps = new Point(0, 0);

		ToggleGroup toggleGroup = new ToggleGroup();

		radioMetal.setToggleGroup(toggleGroup);
		radioOxygen.setToggleGroup(toggleGroup);
		radioBorders.setToggleGroup(toggleGroup);
		radioMetal.setSelected(true);

		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getHeight(), canvas.getWidth());

		buttonLoadBorders.setDisable(true);
		buttonLoadBordersBMP.setDisable(true);
		startButton.setDisable(true);
		buttonVisualize.setDisable(true);

		fieldGridX.setText("10");
		fieldGridY.setText("11");
		fieldConcentration.setText("0.2");

		fieldProbabilityP0.setText("0.25");
		fieldProbabilityP.setText("0.25");
		fieldProbabilityP2.setText("0.25");
		fieldIteratorS1.setText("1");

		fieldMinSquare.setText("3");
		fieldProbabilityPT.setText("0.2");
		fieldFactorR.setText("0.1");

		fieldRadius.setText("2");
		fieldMinSize.setText("5");
		fieldIteratorS2.setText("1");

		fieldIterations.setText("1");
		fieldBorderDiffusion.setText("100.0");

		textDepth.setText("0");
		textIterations.setText("0");

		buttonSaveImage.setDisable(true);
	}


	public void start(ActionEvent actionEvent) {
		double p0, p, p2, pT, factorR, probabilityFactor;
		int minNeighbourSquare, radiusN, sizeGn, iteratorS1, iteratorS2, steps;

		p0 = readDoubleFromTextField(fieldProbabilityP0);
		p = readDoubleFromTextField(fieldProbabilityP) * 2;
		p2 = readDoubleFromTextField(fieldProbabilityP2);
		iteratorS1 = readIntFromTextField(fieldIteratorS1);

		minNeighbourSquare = readIntFromTextField(fieldMinSquare);
		pT = readDoubleFromTextField(fieldProbabilityPT);
		factorR = readDoubleFromTextField(fieldFactorR);

		radiusN = readIntFromTextField(fieldRadius);
		sizeGn = readIntFromTextField(fieldMinSize);
		iteratorS2 = readIntFromTextField(fieldIteratorS2);

		steps = readIntFromTextField(fieldIterations);
		probabilityFactor = readDoubleFromTextField(fieldBorderDiffusion);

		try {
			if (!checkVarInRange(minNeighbourSquare, 1, 8))
				throw new ExceptionWithMessage("Neighbour square has to be in range <1; 8>");

			if (!checkVarInRange(pT, 0.0, 1.0))
				throw new ExceptionWithMessage("Probability pT has to be in range <0.0; 1.0>");

			if (radiusN < 0.0)
				throw new ExceptionWithMessage("Probability pT has to be in range <0.0; 1.0>");

			if (radiusN < 0.0)
				throw new ExceptionWithMessage("Probability pT has to be greater than 0.0");

			if (sizeGn < 0)
				throw new ExceptionWithMessage("Probability pT has to be greater than 0");

			model.startSimulation(minNeighbourSquare, pT, factorR, p0, p2, p, radiusN, sizeGn, iteratorS1, iteratorS2, steps, counterOfSteps, probabilityFactor);
			depth = getDepth(model);

			textDepth.setText(String.valueOf(depth));
			textIterations.setText(String.valueOf(counterOfSteps.x));
		} catch (ExceptionOxygenBottom | ExceptionGrainBorder | ExceptionOxygenDiffusion | ExceptionWithMessage e) {
			showMessage(e.getMessage(), Alert.AlertType.ERROR);
		} catch (Exception e) {
			e.printStackTrace();
		}

		visualizeGrid();
		buttonLoadBorders.setDisable(true);
		buttonLoadBordersBMP.setDisable(true);
		buttonSaveImage.setDisable(false);
	}


	private int getDepth(Model model) {
		int tmpDepth = 0;

		MetalCell[][] grid = model.getGridMetalCell();

		for (int i = model.getHeight() - 1; i >= 0; i--) {
			for (int j = model.getWidth() - 1; j >= 0; j--) {
				if (grid[i][j].getState().equals(State.AO)) {
					tmpDepth = i;
					return tmpDepth;
				}
			}
		}

		return tmpDepth;
	}


	private void visualizeGrid() {
		cleanCanvas();
		if (radioMetal.isSelected())
			showMetalGridOnCanvas();
		else if (radioOxygen.isSelected())
			showOxygenGridOnCanvas();
		else
			showBordersGridOnCanvas();
	}


	private void showMetalGridOnCanvas() {
		if (model != null) {
			MetalCell holdGrid[][] = model.getGridMetalCell();
			cleanCanvas();
			for (int i = 0; i < model.getWidth(); i++) {
				for (int j = 0; j < model.getHeight(); j++) {
					MetalCell holdCell = holdGrid[j][i];
					if (holdCell.getState().equals(State.I)) {
						gc.setFill(Color.LIGHTGRAY);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					} else if (holdCell.getState().equals(State.A)) {
						gc.setFill(Color.RED);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					} else if (holdCell.getState().equals(State.AO)) {
						gc.setFill(Color.DARKRED);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					}
				}
			}
		}
	}


	private void showOxygenGridOnCanvas() {
		if (model != null) {
			OxygenCell holdGrid[][] = model.getGridOxygen();
			cleanCanvas();
			for (int i = 0; i < model.getWidth() - 1; i++) {
				for (int j = 0; j < model.getHeight(); j++) {
					OxygenCell holdCell = holdGrid[j][i];
					if (holdCell.isActive()) {
						gc.setFill(Color.BLACK);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					} else {
						gc.setFill(Color.LIGHTGRAY);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					}
				}
			}

		}
	}


	private void showBordersGridOnCanvas() {
		if (model != null) {
			MetalCell holdGrid[][] = model.getGridMetalCell();
			cleanCanvas();
			for (int i = 0; i < model.getWidth(); i++) {
				for (int j = 0; j < model.getHeight(); j++) {
					MetalCell holdCell = holdGrid[j][i];
					if (holdCell.isBorder()) {
						gc.setFill(Color.BLACK);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					} else {
						gc.setFill(Color.LIGHTGRAY);
						gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
					}
				}
			}
		}
	}


	private void showMessage(String message, Alert.AlertType alertType) {
		Alert alert = new Alert(alertType);
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

		textDepth.setText("0");
		textIterations.setText("0");
		counterOfSteps.x = 0;

		if (checkVarInRange(height, minSizeGrid, maxSizeGrid) && checkVarInRange(width, minSizeGrid, maxSizeGrid)) {
			if (checkVarInRange(concentration, minSizeConcentration, maxSizeConcentration)) {
				model.createAndInitializeGrid(height, width, concentration);
				buttonLoadBorders.setDisable(false);
				buttonLoadBordersBMP.setDisable(false);
				startButton.setDisable(false);
				buttonVisualize.setDisable(false);

				visualizeGrid();
			} else {
				showMessage("Concentration has to be in range <" + minSizeConcentration + "; " + maxSizeConcentration + ">", Alert.AlertType.ERROR);
			}
		} else {
			showMessage("Height and width of the grid has to be in range <" + minSizeGrid + "; " + maxSizeGrid + ">", Alert.AlertType.ERROR);
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
					showMessage(exceptionGrainBorder.getMessage(), Alert.AlertType.ERROR);
				}
			} catch (ExceptionGrainBorder exceptionGrainBorder) {
				outcome = false;
				showMessage(exceptionGrainBorder.getMessage(), Alert.AlertType.ERROR);
			}

			if (outcome) {
				showMessage("Grains borders loaded successfully.", Alert.AlertType.INFORMATION);
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


	private boolean checkVarInRange(int varToCheck, int minRange, int maxRange) {
		return varToCheck >= minRange && varToCheck <= maxRange;
	}


	private boolean checkVarInRange(double varToCheck, double minRange, double maxRange) {
		return varToCheck >= minRange && varToCheck <= maxRange;
	}


	public void visualize(ActionEvent actionEvent) {
		visualizeGrid();
	}


	public void saveImage(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./"));

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("bmp files (*.bmp)", "*.bmp");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showSaveDialog(null);

		if (file != null) {
			try {
				WritableImage writableImage = new WritableImage(model.getWidth(), model.getHeight());
				canvas.snapshot(null, writableImage);
				BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
				ImageIO.write(bufferedImage, "png", file);
			} catch (Exception ignored) {
			}
		}
	}


	public void loadBordersBMP(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("BMP Files", "*.bmp")
		);

		int color;
		int[][] tabOfColors = new int[model.getHeight()][model.getWidth()];
		boolean outcome = true;

		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {
			try {
				clearGrainBorders(model.getHeight(), model.getWidth(), model.getGridMetalCell());
				BufferedImage image = ImageIO.read(selectedFile);

				if (image.getHeight() != model.getHeight() || image.getWidth() != model.getWidth()){
					throw new ExceptionWithMessage("File with grain borders do not fit with grid!");
				}

				for (int xPixel = 0; xPixel < image.getHeight(); xPixel++) {
					for (int yPixel = 0; yPixel < image.getWidth(); yPixel++) {
						color = image.getRGB(xPixel, yPixel);
						tabOfColors[xPixel][yPixel] = color;
					}
				}

				for (int xPixel = 0; xPixel < image.getHeight(); xPixel++) {
					for (int yPixel = 0; yPixel < image.getWidth(); yPixel++) {
						List<Integer> tmpListOfMooreNeighbourColor = getMooreNeighbourMetalCell(xPixel, yPixel,
								image.getHeight(), image.getWidth(), tabOfColors);

						for (Integer el : tmpListOfMooreNeighbourColor){
							if (tabOfColors[xPixel][yPixel] != el){
								model.getGridMetalCell()[xPixel][yPixel].setBorder(true);
								break;
							}
						}
					}
				}

			} catch (ExceptionWithMessage e){
				outcome = false;
				clearGrainBorders(model.getHeight(), model.getWidth(), model.getGridMetalCell());
				showMessage(e.getMessage(), Alert.AlertType.ERROR);
			} catch (Exception ex) {
				outcome = false;
				clearGrainBorders(model.getHeight(), model.getWidth(), model.getGridMetalCell());
				ex.printStackTrace();
			}
		} else {
			outcome = false;
		}

		if (outcome) {
			showMessage("Grains borders loaded successfully.", Alert.AlertType.INFORMATION);
		}
	}


	private List<Integer> getMooreNeighbourMetalCell(int x, int y, int height, int width, int[][] colorGrid) {
		List<Integer> tmpListOfMooreNeighbour = new ArrayList<>();

		//up
		if (x > 0) {
			tmpListOfMooreNeighbour.add(colorGrid[x - 1][y]);
		}

		//up left
		if (x > 0 && y > 0) {
			tmpListOfMooreNeighbour.add(colorGrid[x - 1][y - 1]);
		}

		//up right
		if (x > 0 && y < width - 1) {
			tmpListOfMooreNeighbour.add(colorGrid[x - 1][y + 1]);
		}

		//down
		if (x < height - 1) {
			tmpListOfMooreNeighbour.add(colorGrid[x + 1][y]);
		}

		//down left
		if (x < height - 1 && y > 0) {
			tmpListOfMooreNeighbour.add(colorGrid[x + 1][y - 1]);
		}

		//down right
		if (x < height - 1 && y < width - 1) {
			tmpListOfMooreNeighbour.add(colorGrid[x + 1][y + 1]);
		}

		//left
		if (y > 0) {
			tmpListOfMooreNeighbour.add(colorGrid[x][y - 1]);
		}

		//right
		if (y < width - 1) {
			tmpListOfMooreNeighbour.add(colorGrid[x][y + 1]);
		}

		return tmpListOfMooreNeighbour;
	}
}
