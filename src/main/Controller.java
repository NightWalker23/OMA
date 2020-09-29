package main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import model.exceptions.ExceptionOxygenBottom;
import model.Model;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
	public Button startButton;
	public Canvas canvas;
	GraphicsContext gc;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getHeight(), canvas.getWidth());
	}


	public void start(ActionEvent actionEvent) {
		Model model = new Model();
		model.createAndInitializeGrid(10, 50, 0.1);
		try {
			model.startSimulation((byte) 4, 0.5, 10, 0.7, 0.1, 0.2, 10, 4, 8, 1, 1);
		} catch (ExceptionOxygenBottom e) {
			showErrorMessage(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	private void showErrorMessage(String message){
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText(message);
		alert.showAndWait();
	}


}
