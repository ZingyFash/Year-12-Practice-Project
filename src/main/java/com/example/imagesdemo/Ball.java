package com.example.imagesdemo;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Random;

public class Ball {
    Vector2 pos;
    Vector2 vel;
    public Circle gfx;
    public final ArrayList<Ball> inContactBalls = new ArrayList<>();

    public static void main(String[] args) {
        MainApp.main(args);
    }
    Ball() {
        Random random = new Random();
        int r = random.nextInt(15)+1 << 4;
        int g = random.nextInt(15)+1 << 4;
        int b = random.nextInt(15)+1 << 4;

        r = r << 16;
        g = g << 8;

        StringBuilder colourString = new StringBuilder(Integer.toHexString(r + g + b));
        int length = colourString.length();
        for (int i = 0; i < 6 - length; i++) {
            colourString.insert(0, "0");
        }
        gfx = new Circle(0, 0, 20, Paint.valueOf("#"+ colourString));
        MainApp.ROOT.getChildren().add(gfx);


        int wall = random.nextInt(4);
        double percent = random.nextDouble();
        double x = ((wall & 1) == 0)? percent*MainApp.SCENE_WIDTH :
                ((wall & 2) == 0)? gfx.getRadius()+5 : MainApp.SCENE_WIDTH - gfx.getRadius()+5;
        double y = ((wall & 1) == 1)? percent*MainApp.SCENE_HEIGHT :
                ((wall & 2) == 0)? gfx.getRadius()+5 : MainApp.SCENE_HEIGHT - gfx.getRadius()+5;
        pos = Vector2.vec2(x, y);
        vel = Vector2.vec2(3*(random.nextDouble()-.5), 3*(random.nextDouble()-.5));

        gfx.setCenterX(pos.x);
        gfx.setCenterY(pos.y);
    }
    Ball(String s) {
        System.out.print(s);
    }

    public void update() {
        resolveBorderCollisions();
        resolveBallCollisions(false);
        curveToPlayer();
        pos = Vector2.add(pos, vel);
        gfx.setCenterX(pos.x);
        gfx.setCenterY(pos.y);
    }

    private void curveToPlayer() {
        Vector2 displacement = Vector2.sub(MainApp.BALLS.getLast().pos, pos);
        double distance = displacement.getMagnitude();
        displacement = displacement.normalise();
        vel = Vector2.add(vel, Vector2.mul(displacement, 5/distance));
    }

    void resolveBallCollisions(boolean testing) {
        MainApp.BALLS.forEach((b) -> {
            if (this == b) return;
            Vector2 displacement = Vector2.sub(b.pos, pos);
            double distance = displacement.getMagnitude();
            if (distance > gfx.getRadius() + b.gfx.getRadius()) {
                inContactBalls.remove(b);
                return;
            }
            if (b == MainApp.BALLS.getLast()) MainApp.endGame();
            if (this.vel == Vector2.ZERO) {
                System.out.println();
            }
            if (inContactBalls.contains(b)) return;
            else inContactBalls.add(b);
            double a = Math.acos(Vector2.dot(Vector2.I, displacement.normalise()));
            if (displacement.y > 0) a = -a;
            Vector2 u1 = vel.rotate(a);
            Vector2 u2 = MainApp.ballVelHash.get(b).rotate(a);
            u1.x = u2.x;
            vel = u1.rotate(-a);

            if (!testing) b.resolveBallCollisions(true);
        });
    }

    void resolveBorderCollisions() {
        if (pos.x - gfx.getRadius() < 0) vel.x = Math.abs(vel.x);
        if (pos.x + gfx.getRadius() > MainApp.SCENE_WIDTH) vel.x = -Math.abs(vel.x);
        if (pos.y - gfx.getRadius() < 0) vel.y = Math.abs(vel.y);
        if (pos.y + gfx.getRadius() > MainApp.SCENE_HEIGHT) vel.y = -Math.abs(vel.y);
    }

    Vector2 getVel() {
        return vel;
    }
}
