package com.roman.speedcore;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {

    private final Queue<Float> window;
    private final int size;
    private float sum;

    public MovingAverage(int size) {
        this.size = size;
        this.window = new LinkedList<>();
        this.sum = 0f;
    }

    public void add(float value) {
        sum += value;
        window.add(value);
        if (window.size() > size) {
            sum -= window.poll();
        }
    }

    public float getAverage() {
        if (window.isEmpty()) {
            return 0f;
        }
        return sum / window.size();
    }

    public static class CircularMovingAverage {
        private final int size;
        private final Queue<Float> window = new LinkedList<>();
        private float sumSin;
        private float sumCos;

        public CircularMovingAverage(int size) {
            this.size = size;
        }

        public void add(float angle) {
            float angleRad = (float) Math.toRadians(angle);
            sumSin += (float) Math.sin(angleRad);
            sumCos += (float) Math.cos(angleRad);
            window.add(angle);

            if (window.size() > size) {
                float oldAngle = window.poll();
                float oldAngleRad = (float) Math.toRadians(oldAngle);
                sumSin -= (float) Math.sin(oldAngleRad);
                sumCos -= (float) Math.cos(oldAngleRad);
            }
        }

        public float getAverage() {
            if (window.isEmpty()) {
                return 0f;
            }
            float avgSin = sumSin / window.size();
            float avgCos = sumCos / window.size();
            float avgAngleRad = (float) Math.atan2(avgSin, avgCos);
            return (float) Math.toDegrees(avgAngleRad);
        }
    }
}
