package com.example.imagesdemo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class MainApp extends Application {

    public static final Group ROOT = new Group();
    private static Player player = new Player();
    public static final ArrayList<Ball> BALLS = new ArrayList<>();
    public static final int SCENE_WIDTH = 1000;
    public static final int SCENE_HEIGHT = 750;

    private static double timerCounter = 0;
    private static double scoreCounter = 0;
    private static Label timerLabel;
    static Timer timer;

    // Stores the velocities of the balls at the start of the update
    // so that calculations are not affected by the order that they are done in
    static HashMap<Ball, Vector2> ballVelHash = new HashMap<>();
    private static boolean isRunning = true;
    private static boolean isTimeAttack = true;
    private static final double kineticEnergyMultiplier = 0.00025;

    /**
     * Handles the logic for ending the game. This includes updating the player's
     * recorded times, writing the data back to the file *to be changed when a database is used*
     * and displaying the end screen
     */
    public static void endGame() {
        if (isTimeAttack) {
            times.put(name, new double[]{Double.max(times.get(name)[0], timerCounter),
                    Double.min(times.get(name)[1], timerCounter),
                    times.get(name)[2]
            });
        } else {
            times.put(name, new double[]{times.get(name)[0],
                    times.get(name)[1],
                    Double.max(times.get(name)[2], scoreCounter)
            });
        }
        writeTimesToFile();

        displayEndScreen();
    }

    /**
     * Writes the times data back to the times file to update any changes that occurred
     */
    private static void writeTimesToFile() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("Times.txt", false));
            String[] lines = new String[times.size()];
            final int[] i = {0};
            times.forEach((s, d) -> {
                lines[i[0]] = s;
                for (double v : d) {
                    lines[i[0]] += ","+v;
                }
                i[0]++;
            });
            for (String line : lines) {
                writer.write(line+"\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to write times to file.");
        }
    }

    /**
     * Shows the player's time and provided the user with buttons to exit the application or restart it
     */
    private static void displayEndScreen() {
        isRunning = false;
        timerLabel.setTranslateX(450);
        timerLabel.setTranslateY(750/2.0 - 50);

        Button exitButton = genButton("Exit", Paint.valueOf("#ff0000"), Paint.valueOf("#00f0ff"), 450, 450);
        exitButton.setOnAction(e -> System.exit(0));

        Button restartButton = genButton("Restart", Paint.valueOf("#00f0ff"), Paint.valueOf("#ff0000"), 450, 550);
        restartButton.setOnAction(e -> {
            ROOT.getChildren().clear();
            BALLS.clear();

            timerCounter = 0;

            for (int i = 0; i < 7; i++) {
                BALLS.add(new Ball());
            }

            player = new Player();
            BALLS.add(player);


            isRunning = true;

            timerLabel = new Label();
            timerLabel.setBackground(Background.fill(Paint.valueOf("#ffffff")));
            timerLabel.setTextFill(Paint.valueOf("#ff0000"));
            timerLabel.setFont(Font.font(50));
            timerLabel.setMinWidth(100);
            ROOT.getChildren().add(timerLabel);
        });
    }

    /**
     * A helper function which generates and returns a button to make other functions
     * more readable. NOTE : the functionality of the button still needs to be
     * implemented in the calling function.
     * @param text - The text to be displayed on the button
     * @param textColour - The colour of the text on the button
     * @param backgroundColour - The colour of the background of the button
     * @param posX - The displacement in the x-axis of the button in the scene
     * @param posY - The displacement in the y-axis of the button in the scene
     * @return The newly generated button
     */
    private static Button genButton(String text, Paint textColour, Paint backgroundColour, double posX, double posY) {
        Button button = new Button(text);
        button.setTextFill(textColour);
        button.setBackground(Background.fill(backgroundColour));
        button.setTranslateX(posX);
        button.setTranslateY(posY);
        button.setFont(new Font(50));
        ROOT.getChildren().add(button);
        return button;
    }

    /**
     * Sets up the scene, balls, and event handlers for the scene. Also provided the user a button to run the game
     * @param stage - the stage to set the scene in.
     */
    private static void initialiseApplication(Stage stage) {
        Scene scene = new Scene(ROOT, SCENE_WIDTH,SCENE_HEIGHT);
        scene.setFill(Paint.valueOf("#000000"));

        for (int i = 0; i < 7; i++) {
            BALLS.add(new Ball());
        }

        BALLS.add(player);

        TextField tf = new TextField();
        tf.setTranslateX(400);
        tf.setTranslateY(150);

        ROOT.getChildren().add(tf);

        Button timeAttackButton = genButton("Run Time Attack", Paint.valueOf("#00ff00"),
                Paint.valueOf("#ff00ff"), 350, 300);
        Button scoreAttackButton = genButton("Run Score Attack", Paint.valueOf("#ffffff"),
                Paint.valueOf("#555555"), 350, 500);
        timeAttackButton.setOnAction(e -> {
            ROOT.getChildren().removeAll(tf, timeAttackButton, scoreAttackButton);
            run(tf.getText());
        });
        scoreAttackButton.setOnAction(e -> {
            isTimeAttack = false;
            ROOT.getChildren().removeAll(tf, timeAttackButton, scoreAttackButton);
            run(tf.getText());
        });

        EventHandler<KeyEvent> keyPressedEventHandler = MainApp::move;
        EventHandler<KeyEvent> keyReleasedEventHandler = MainApp::stopMove;
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedEventHandler);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedEventHandler);

        stage.setOnCloseRequest(event -> System.exit(0));

        stage.setTitle("Practice Project");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Handles the logic for starting the game. Initialises a timer and a label to keep track of the time
     * elapsed.
     */
    private static void run(String name) {
        MainApp.name = name;

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("Times.txt"));
            String line = reader.readLine();
            while (line != null) {
                times.put(line.split(",")[0],
                        new double[]{Double.parseDouble(line.split(",")[1]),
                                Double.parseDouble(line.split(",")[2]),
                                Double.parseDouble(line.split(",")[3])});
                line = reader.readLine();
            }
            reader.close();
        } catch(IOException e) {
            System.out.println("Failed to read the times.");
        }

        if (!times.containsKey(name)) {
            times.put(name, new double[]{0, 1000, 0});
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(MainApp::update);
            }
        }, 0, 10);

        timerLabel = new Label();
        timerLabel.setBackground(Background.fill(Paint.valueOf("#ffffff")));
        timerLabel.setTextFill(Paint.valueOf("#ff0000"));
        timerLabel.setFont(Font.font(50));
        timerLabel.setMinWidth(100);
        if (!isTimeAttack) timerLabel.setMinWidth(125);
        ROOT.getChildren().add(timerLabel);

    }

    @Override
    public void start(Stage stage) {

        initialiseApplication(stage);

    }

    private static void move(KeyEvent keyEvent) {
        player.updateKeyCodeHashMap(keyEvent.getCode(), true);
    }

    private static void stopMove(KeyEvent keyEvent) {
        player.updateKeyCodeHashMap(keyEvent.getCode(), false);
    }

    /**
     * Called once every update, updates the velocities hash map and calls the balls update functions.
     * Then increases the timer counter and formats it for the label to avoid floating point imprecision.
     */
    public static void update() {
        if (!isRunning) return;
        player.update();
        BALLS.forEach(b -> ballVelHash.put(b, b.getVel()));
        BALLS.forEach(Ball::update);
        if (isTimeAttack) {
            timerCounter += .01;
            timerCounter = Math.round(timerCounter * 100) / 100.0;
            if (timerCounter % 10 != timerCounter) {
                timerLabel.setMinWidth(125);
            }
            timerLabel.setText(Double.toString(timerCounter));
        } else {
            scoreCounter += calculateKE() * kineticEnergyMultiplier;
            scoreCounter = Math.round(scoreCounter * 1000) / 1000.0;
            if (scoreCounter % 10 != scoreCounter) {
                timerLabel.setMinWidth(150);
            }
            timerLabel.setText(Double.toString(scoreCounter));
        }
    }

    private static double calculateKE() {
        double[] total = {0};
        BALLS.forEach(ball -> total[0] += ball.vel.getMagnitude() * ball.vel.getMagnitude());
        return total[0];
    }

    private static String name;
    private static final HashMap<String, double[]> times = new HashMap<>();

    /**
     * For now handles the identification of the user then runs the JavaFX application.
     * @param args - command line arguments or something I don't know it just complained
     *            that there was nothing here.
     */
    public static void main(String[] args) {
        launch();
    }
}