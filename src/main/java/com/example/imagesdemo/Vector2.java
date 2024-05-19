package com.example.imagesdemo;

public class Vector2 {
    public static final Vector2 I = new Vector2(1, 0);
    public static final Vector2 ZERO = new Vector2(0, 0);
    double x, y;

    Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    static Vector2 vec2(double x, double y) {
        return new Vector2(x, y);
    }

    static Vector2 add(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }

    static Vector2 sub(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    static Vector2 mul(Vector2 a, Vector2 b) {
        return new Vector2(a.x * b.x, a.y * b.y);
    }
    static Vector2 mul(Vector2 a, double s) {
        return new Vector2(a.x * s, a.y * s);
    }

    static Vector2 div(Vector2 a, Vector2 b) {
        return new Vector2(a.x / b.x, a.y / b.y);
    }

    static double dot(Vector2 a, Vector2 b) {
        return a.x * b.x + a.y * b.y;
    }

    Vector2 normalise() {
        double magnitude = getMagnitude();
        return new Vector2(x / magnitude, y / magnitude);
    }

    double getMagnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Returns the resulting vector when this vector is rotated
     * by an angle of a radians
     *
     * @param   a   The angle to rotate this vector by.
     * @return  The new, rotated vector.
     */
    Vector2 rotate(double a) {
        double s = Math.sin(a);
        double c = Math.cos(a);
        return vec2(x*c-y*s, x*s+y*c);
    }
}
