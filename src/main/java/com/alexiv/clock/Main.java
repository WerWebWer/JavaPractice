package com.alexiv.clock;

public class Main {

    private static final long SLEEP = 2000;

    public static void main(String[] args) {
        Clock clock = new Clock(0,0,100);
        clock.start();

        sleep(1000);

        clock.stop();
    }

    private static void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
