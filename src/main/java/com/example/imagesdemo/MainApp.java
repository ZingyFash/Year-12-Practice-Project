package com.example.imagesdemo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    private static Stage STAGE;
    private static Scene scene;
    private static Player player;
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
    private static final double scoreAttackMultiplier = 0.00025;

    /**
     * Handles the logic for ending the game. This includes updating the player's
     * recorded times, writing the data back to the file *to be changed when a database is used*
     * and displaying the end screen
     */
    public static void endGame() {
        if (isTimeAttack) {
            times.put(name, new double[]{Double.max(times.get(name)[0], timerCounter),
                    Double.min(times.get(name)[1], timerCounter),
                    times.get(name)[2], times.get(name)[3]
            });
        } else {
            times.put(name, new double[]{times.get(name)[0],
                    times.get(name)[1],
                    Double.max(times.get(name)[2], scoreCounter),
                    Double.min(times.get(name)[3], scoreCounter)
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
            scoreCounter = 0;

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

        Button modeSelectButton = genButton("Choose Game Mode", Paint.valueOf("00ff00"), Paint.valueOf("#0000ff"), 400, 650);
        modeSelectButton.setOnAction(e -> {
            ROOT.getChildren().clear();
            BALLS.clear();
            initialiseApplication();
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
     */
    private static void initialiseApplication() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("Times.txt"));
            String line = reader.readLine();
            while (line != null) {
                times.put(line.split(",")[0],
                        new double[]{Double.parseDouble(line.split(",")[1]),
                                Double.parseDouble(line.split(",")[2]),
                                Double.parseDouble(line.split(",")[3]),
                                Double.parseDouble(line.split(",")[4])
                        });
                line = reader.readLine();
            }
            reader.close();
        } catch(IOException e) {
            System.out.println("Failed to read the times.");
        }

        scene.setFill(Paint.valueOf("#000000"));
        timerCounter = 0;
        scoreCounter = 0;
        for (int i = 0; i < 7; i++) {
            BALLS.add(new Ball());
        }
        player = new Player();

        BALLS.add(player);

        TextField tf = new TextField();
        tf.setTranslateX(600);
        tf.setTranslateY(300);

        ROOT.getChildren().add(tf);

        TextField[] textFields = new TextField[8];
        for (int i = 0; i < textFields.length; i++) {
            textFields[i] = new TextField();
            textFields[i].setTranslateX(700);
            textFields[i].setTranslateY(50+50*i + ((i%4==i)?0:200));
            ROOT.getChildren().add(textFields[i]);
        }
        textFields[0].setText(getBestTime());
        textFields[1].setText(getLeastTime());
        textFields[2].setText(getBestScore());
        textFields[3].setText(getLeastScore());
        ObservableList<String> options = FXCollections.observableArrayList(times.keySet());
        options.add("New Guy");
        ComboBox<String> getScores = new ComboBox<>(options);
        getScores.setTranslateX(600);
        getScores.setTranslateY(400);
        getScores.setOnAction(e -> {
            if (getScores.getValue().equals("New Guy")) {
                textFields[4].setText("");
                textFields[5].setText("");
                textFields[6].setText("");
                textFields[7].setText("");
                return;
            }
            textFields[4].setText(String.valueOf(times.get(getScores.getValue())[0]));
            textFields[5].setText(String.valueOf(times.get(getScores.getValue())[1]));
            textFields[6].setText(String.valueOf(times.get(getScores.getValue())[2]));
            textFields[7].setText(String.valueOf(times.get(getScores.getValue())[3]));
        });

        ROOT.getChildren().add(getScores);

        Button timeAttackButton = genButton("Run Time Attack", Paint.valueOf("#00ff00"),
                Paint.valueOf("#ff00ff"), 150, 200);
        Button scoreAttackButton = genButton("Run Score Attack", Paint.valueOf("#ffffff"),
                Paint.valueOf("#555555"), 150, 400);
        timeAttackButton.setOnAction(e -> {
            isTimeAttack = true;
            String s = (getScores.getValue().equals("New Guy"))?tf.getText():getScores.getValue();
            ROOT.getChildren().removeAll(tf, timeAttackButton, scoreAttackButton, getScores);
            for (TextField textField : textFields) {
                ROOT.getChildren().remove(textField);
            }
            run(s);
        });
        scoreAttackButton.setOnAction(e -> {
            isTimeAttack = false;
            String s = (getScores.getValue().equals("New Guy"))?tf.getText():getScores.getValue();
            ROOT.getChildren().removeAll(tf, timeAttackButton, scoreAttackButton, getScores);
            for (TextField textField : textFields) {
                ROOT.getChildren().remove(textField);
            }
            run(s);
        });

        EventHandler<KeyEvent> keyPressedEventHandler = MainApp::move;
        EventHandler<KeyEvent> keyReleasedEventHandler = MainApp::stopMove;
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedEventHandler);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedEventHandler);

        STAGE.setOnCloseRequest(event -> System.exit(0));

        STAGE.setTitle("Practice Project");
        STAGE.setScene(scene);
        STAGE.show();

    }

    private static String getBestTime() {
        String[] bestTimeString = {""};
        double[] bestTime = {0};
        times.forEach((s,d) -> {
            if (d[0] > bestTime[0]) {
                bestTime[0] = d[0];
                bestTimeString[0] = s+" : "+bestTime[0];
            }
        });
        return bestTimeString[0];
    }

    private static String getLeastTime() {
        String[] leastTimeString = {""};
        double[] leastTime = {1000};
        times.forEach((s,d) -> {
            if (d[1] < leastTime[0]) {
                leastTime[0] = d[1];
                leastTimeString[0] = s+" : "+leastTime[0];
            }
        });
        return leastTimeString[0];
    }

    private static String getBestScore() {
        String[] bestScoreString = {""};
        double[] bestScore = {0};
        times.forEach((s,d) -> {
            if (d[2] > bestScore[0]) {
                bestScore[0] = d[2];
                bestScoreString[0] = s+" : "+bestScore[0];
            }
        });
        return bestScoreString[0];
    }

    private static String getLeastScore() {
        String[] leastScoreString = {""};
        double[] leastScore = {1000};
        times.forEach((s,d) -> {
            if (d[3] < leastScore[0]) {
                leastScore[0] = d[3];
                leastScoreString[0] = s+" : "+leastScore[0];
            }
        });
        return leastScoreString[0];
    }

    /**
     * Handles the logic for starting the game. Initialises a timer and a label to keep track of the time
     * elapsed.
     */
    private static void run(String name) {
        MainApp.name = name;

        if (!times.containsKey(name)) {
            times.put(name, new double[]{0, 1000, 0, 1000});
        }

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(MainApp::update);
                }
            }, 0, 10);
        }
        if (timerLabel == null) {
            timerLabel = new Label();
            timerLabel.setBackground(Background.fill(Paint.valueOf("#ffffff")));
            timerLabel.setTextFill(Paint.valueOf("#ff0000"));
            timerLabel.setFont(Font.font(50));
        }
        timerLabel.setTranslateX(0);
        timerLabel.setTranslateY(0);
        timerLabel.setMinWidth(100);
        if (!isTimeAttack) timerLabel.setMinWidth(125);
        ROOT.getChildren().add(timerLabel);
        isRunning = true;
    }

    @Override
    public void start(Stage stage) {
        STAGE = stage;
        scene = new Scene(ROOT, SCENE_WIDTH,SCENE_HEIGHT);
        initialiseApplication();

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
            scoreCounter += calculateScore() * scoreAttackMultiplier;
            scoreCounter = Math.round(scoreCounter * 1000) / 1000.0;
            if (scoreCounter % 10 != scoreCounter) {
                timerLabel.setMinWidth(150);
            }
            timerLabel.setText(Double.toString(scoreCounter));
        }
    }

    private static double calculateScore() {
        double[] total = {0};
        BALLS.forEach(ball -> total[0] += (ball.vel.getMagnitude() * ball.vel.getMagnitude()) / Math.log1p(ball.vel.getMagnitude() + 1));
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