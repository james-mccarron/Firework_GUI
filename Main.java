package Assignment5_17jpm5;

import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

/**
 * Class that creates a window an incorporates the fxml file.
 * @author James McCarron
 * @version 1.0
 */
public class Main extends Application{
	/**
	 * Class that creates a window with specific dimensions and calls an fxml file with customizations to the window.
	 * @param primaryStage The window being opened.
	 * @throws Exception If the window does not open.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			//identifies the fxml file uses the get resources to load it
			GridPane root = (GridPane)FXMLLoader.load(getClass().getResource("sample.fxml"));
			Scene scene=new Scene(root, 736,434);
			primaryStage.setScene(scene);
			primaryStage.setTitle("James McCarron's Program (17jpm5)");
			primaryStage.show();
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Invokes the inherited launch() method to start the application
	 * @param args The supplied args from the main parameter list to launch()
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
