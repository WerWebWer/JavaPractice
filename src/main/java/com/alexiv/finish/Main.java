package com.alexiv.finish;

import com.alexiv.finish.client.ClientUI;
import com.alexiv.finish.server.ServerUI;
import com.alexiv.utils.Logger;

import javax.swing.*;

public class Main implements Runnable{

    JFrame theFrame;

    public Main(JFrame f) {
        this.theFrame = f;
        Logger.d("wow");
    }

    public static void main(String[] args) {
        JFrame f1 = new ServerUI();
        JFrame f2 = new ClientUI();
        //JFrame f3 = new ClientUI();

        Thread t1 = new Thread(new Main(f1));
        Thread t2 = new Thread(new Main(f2));
        //Thread t3 = new Thread(new Main(f3));

        t1.start();
        t2.start();
        //t3.start();
    }

    @Override
    public void run() {
        Logger.d("run");
        theFrame.setVisible(true);
        // Attention: This closes the app, and therefore both frames!
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
