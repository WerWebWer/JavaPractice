package com.alexiv.finish.client;

public class StartClients {
    private static final int COUNT_CLIENTS  = 2;

    public static void main(String[] args) {
        for (int i = 0; i < COUNT_CLIENTS; i++) {
            new Thread(ClientUI::new).start();
        }
    }

}
