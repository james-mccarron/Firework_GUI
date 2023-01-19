package Assignment5_17jpm5;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import javafx.application.Platform;

/**
 * Class that incorporates the physics of the fireworks being launched, and customizes them with different attributes in the window
 * @author James McCarron
 * @version 1.0
 */
public class Controller {

    //initializing all the attributes from the scene builder.

    @FXML
    private Label labelWindSpeed;

    @FXML
    private Label labelLaunchAngle;

    @FXML
    private Canvas canvas;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button buttonStart;

    @FXML
    private Button buttonReset;

    @FXML
    private Button buttonExit;

    @FXML
    private Slider sliderWindSpeed;

    @FXML
    private Slider sliderLaunchAngle;

    //variables used throughout the program.
    private Timeline timeline;
    private GraphicsContext graphicsContext;
    private ParticleManager particleManager;
    private ArrayList<Particle> fireworks;
    private double canvasWidth;
    private double canvasHeight;
    private int launchAngle;
    private double desiredFrameRate = 60;
    private double startTime;

    //changes the texts of the two labels when the start button is clicked.
    @FXML
    void begin(ActionEvent event){
        labelWindSpeed.setText("Touchdown!");
        labelLaunchAngle.setText("Launching!");
    }

    //exits the program when the exit program is clicked.
    @FXML
    void exitProgram(MouseEvent event){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void resetProgram(MouseEvent event){}

    //starts firing the fireworks from the launch tube when the start button is clicked.
    @FXML
    void clickStart(MouseEvent event) throws EmitterException, EnvironmentException {
        timeline = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> drawFireworks()), new KeyFrame(Duration.millis(1000 / desiredFrameRate)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        launchAngle = (int) sliderLaunchAngle.getValue();
        int windSpeed = (int) sliderWindSpeed.getValue();
        particleManager = new ParticleManager(windSpeed, launchAngle);
        particleManager.start(0);
        startTime = System.currentTimeMillis() / 1000.0;
        fireworks = particleManager.getFireworks(0);
        timeline.playFromStart();
    }
    //resets the text on the labels when the reset button is clicked.
    @FXML
    void reset(ActionEvent event){
        labelWindSpeed.setText("Wind Speed");
        labelLaunchAngle.setText("Launch Angle");
        Platform.exit();
    }
    //changes the texts when the end button is clicked.
    @FXML
    void terminate(ActionEvent event){
        labelWindSpeed.setText("Program Finished");
        labelLaunchAngle.setText("Goodbye!");
    }

    //creates an empty canvas when the program is launched.
    @FXML
    void emptyCanvas(){
        canvasHeight = canvas.getHeight();
        canvasWidth = canvas.getWidth();
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
    }

    //creates fireworks and spark emitters of various colours.
    @FXML
    void drawFireworks(){
        double scaleFactor;
        double realHeight = 22;
        double launchAngleNow = Math.PI * launchAngle / 180;
        double tubeHeight = 1.0;
        double tubeWidth = 0.5;
        double x1, y1, xTip,yTip;
        double[] position;
        double size, pixelX, pixelY;

        if(fireworks.size() == 0){
            timeline.stop();;
            return;
        }
        emptyCanvas();
        scaleFactor = canvasHeight / realHeight;
        graphicsContext.setStroke(Color.YELLOW);
        graphicsContext.setLineWidth(tubeWidth * scaleFactor);
        x1 = canvasWidth / 2;
        y1 = canvasHeight;
        xTip = x1 + scaleFactor * tubeHeight * Math.sin(launchAngleNow);
        yTip = y1 - scaleFactor * tubeHeight * Math.cos(launchAngleNow);
        graphicsContext.strokeLine(x1, y1, xTip, yTip);
        fireworks = particleManager.getFireworks(System.currentTimeMillis() / 1000.0 - startTime);

        for(Particle firework : fireworks){
            position = firework.getPosition();
            graphicsContext.setFill(firework.getColour());

            if(firework instanceof Star)
                size = 6;
            else
                size = 2;
            pixelX = position[0] * scaleFactor + canvasWidth / 2 - size / 2;
            pixelY = canvasHeight - position[1] * scaleFactor - size / 2;

            if(firework instanceof LaunchSpark){
                graphicsContext.setStroke(firework.getColour());
                graphicsContext.setLineWidth(2);
                graphicsContext.strokeLine(xTip, yTip, pixelX, pixelY);
            }
            graphicsContext.fillOval(pixelX, pixelY, size, size);

            if(firework instanceof Star){
                graphicsContext.setGlobalAlpha(0.5);
                graphicsContext.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, firework.getColour()), new Stop(1.0, Color.BLACK)));
                graphicsContext.fillOval(pixelX - size, pixelY - size, 3 * size, 3 * size);
                graphicsContext.setGlobalAlpha(1.0);
            }
        }
    }

    //initializes the window and the two sliders.
    @FXML
    void initialize(){
        double launchAngleNew = Math.PI * launchAngle / 180.0;
        double[] position = {Math.sin(launchAngleNew), Math.cos(launchAngleNew)};
        borderPane.widthProperty().addListener(((observableValue, oldWidth, newWidth) ->
        {
            canvas.setWidth((Double) newWidth);
            emptyCanvas();
        }));
        borderPane.widthProperty().addListener((observableValue, oldWidth, newWidth) ->
        {
            canvas.setHeight((Double) newWidth - 58);
            emptyCanvas();
        });
        sliderLaunchAngle.valueProperty().addListener(((observableValue, oldVal, newVal) ->
        {
            if(particleManager == null)
                return;
            launchAngle = newVal.intValue();
            particleManager.setLaunchAngle(newVal.doubleValue());
            particleManager.setTipPosition(position);
        }));
        sliderWindSpeed.valueProperty().addListener((observableValue, oldVal, newVal) ->
        {
            if(particleManager == null)
                return;
            particleManager.setWindVelocity(newVal.doubleValue());
        });
    }
}
