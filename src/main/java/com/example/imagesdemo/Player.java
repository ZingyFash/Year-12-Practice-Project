package com.example.imagesdemo;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.HashMap;

public class Player extends Ball {

    private final HashMap<KeyCode, Boolean> keyCodeHashMap;
    private final Circle core;

    public static void main(String[] args) {
        MainApp.main(args);
    }

    /**
     * Initialises player position, velocity, graphics and keyCodeHashMap.
     * Initial values are then places in the hash map
     */
    Player() {
        super("");
        this.pos = Vector2.vec2(500, 500);
        this.vel = Vector2.vec2(0, 0);
        this.gfx = new Circle(this.pos.x, this.pos.y, 25, Paint.valueOf("#ff0000"));
        this.core = new Circle(this.pos.x, this.pos.y, 15, Paint.valueOf("#ffffff"));
        MainApp.ROOT.getChildren().addAll(this.gfx, this.core);
        keyCodeHashMap = new HashMap<>();
        keyCodeHashMap.put(KeyCode.W, false);
        keyCodeHashMap.put(KeyCode.A, false);
        keyCodeHashMap.put(KeyCode.S, false);
        keyCodeHashMap.put(KeyCode.D, false);
        keyCodeHashMap.put(KeyCode.UP, false);
        keyCodeHashMap.put(KeyCode.DOWN, false);
        keyCodeHashMap.put(KeyCode.LEFT, false);
        keyCodeHashMap.put(KeyCode.RIGHT, false);
    }

    /**
     * This updates the values in the keyCodeHashMap, containing information about which
     * keys are currently being pressed. Called whenever a key is pressed or released.
     *
     * @param code - The key code indicating which key whose status is being updated
     * @param bool - Indicates whether the key has been pressed (true) or released (false)
     */
    public void updateKeyCodeHashMap(KeyCode code, Boolean bool) {
        keyCodeHashMap.replace(code, bool);
    }

    /**
     * This is called once every frame and handles the collisions before updating position based off of velocity.
     */
    public void update() {
        checkInputs();
        pos = Vector2.add(pos, vel);
        resolveBorderCollisions();
        gfx.setCenterX(pos.x);
        gfx.setCenterY(pos.y);
        core.setCenterX(pos.x);
        core.setCenterY(pos.y);
    }

    /**
     * Clamps the player's position to the bounds of the screen
     */
    void resolveBorderCollisions () {
        pos.x = Math.max(gfx.getRadius(), pos.x);
        pos.x = Math.min(pos.x, MainApp.SCENE_WIDTH - gfx.getRadius());
        pos.y = Math.max(0 + gfx.getRadius(), pos.y);
        pos.y = Math.min(pos.y, MainApp.SCENE_HEIGHT - gfx.getRadius());
    }

    /**
     * Handles the inputs stored in the keyCodeHashMap and updating the velocity accordingly
     */
    private void checkInputs () {
        if (keyCodeHashMap.get(KeyCode.W) || keyCodeHashMap.get(KeyCode.UP)) vel.y = -3;
        if (keyCodeHashMap.get(KeyCode.A) || keyCodeHashMap.get(KeyCode.LEFT)) vel.x = -3;
        if (keyCodeHashMap.get(KeyCode.S) || keyCodeHashMap.get(KeyCode.DOWN)) vel.y = 3;
        if (keyCodeHashMap.get(KeyCode.D) || keyCodeHashMap.get(KeyCode.RIGHT)) vel.x = 3;
        if (!(keyCodeHashMap.get(KeyCode.W) || keyCodeHashMap.get(KeyCode.UP)) && !(keyCodeHashMap.get(KeyCode.S) || keyCodeHashMap.get(KeyCode.DOWN))) vel.y = 0;
        if (!(keyCodeHashMap.get(KeyCode.A) || keyCodeHashMap.get(KeyCode.LEFT)) && !(keyCodeHashMap.get(KeyCode.D)|| keyCodeHashMap.get(KeyCode.RIGHT))) vel.x = 0;
    }
}

