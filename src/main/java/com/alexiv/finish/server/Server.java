package com.alexiv.finish.server;

import com.alexiv.finish.utils.Alarm;
import com.alexiv.finish.utils.Time;
import com.alexiv.utils.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import static com.alexiv.finish.utils.Constants.ALARM;
import static com.alexiv.finish.utils.Constants.PORT;

public class Server {
    private static final String TAG = Server.class.getSimpleName();

    private final Alarm mMyTimer = new Alarm();

    public static LinkedList<ServerLogic> serverList = new LinkedList<>(); // список всех нитей
    public static LinkedList<ServerLogic.ServerLogicCallback> serverCallback = new LinkedList<>();

    private ServerCallback mCallback = time -> mMyTimer.addAlarm(time);
    private ServerUI.ServerUICallback mUICallback;

    private Alarm.MyTimerActions mTimerCallback = new Alarm.MyTimerActions() {
        @Override
        public void action(Time time) {
            mUICallback.updateTimer(time);
            for(ServerLogic.ServerLogicCallback callback : serverCallback) {
                callback.action(time);
            }
        }

        @Override
        public void startAction() {
            //no op
        }

        @Override
        public void pauseAction() {
            //no op
        }

        @Override
        public void stopAction() {
            mUICallback.end();
        }

        @Override
        public void endAction() {
            mUICallback.end();
        }

        @Override
        public void alarm() {
            for(ServerLogic.ServerLogicCallback callback : serverCallback) {
                callback.alarm();
            }
        }
    };

    interface ServerCallback {
        void addAlarm(Time time);
    }

    public Server(ServerUI.ServerUICallback callback) {
        mUICallback = callback;
        mMyTimer.addMyTimerActions(mTimerCallback);
        Thread thread = new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(PORT);
                try {
                    while (true) { // Блокируется до возникновения нового соединения:
                        Socket socket = server.accept();
                        try {
                            ServerLogic sl = new ServerLogic(socket);
                            sl.setCallback(mCallback);
                            serverCallback.add(sl.getActionCallback());
                            serverList.add(sl); // добавить новое соединенние в список
                        } catch (IOException e) {
                            socket.close();
                        }
                    }
                } finally {
                    server.close();
                }
            } catch (IOException e) {
                Logger.d(TAG, "something wrong");
            }
        });
        thread.start();
    }

    public void setTimeTimer(int h, int m, int s) {
        mMyTimer.setTime(h, m, s);
    }

    public void start() {
        mMyTimer.start();
    }

    public void pauseResume() {
        switch (mMyTimer.getStatus()) {
            case RUN -> mMyTimer.pause();
            case PAUSE -> mMyTimer.resume();
        }
    }

    public void stop() {
        mMyTimer.stop();
    }
}

class ServerLogic extends Thread {
    private static final String TAG = ServerLogic.class.getSimpleName();

    private BufferedReader mIn; // поток чтения из сокета
    private BufferedWriter mOut; // поток записи в сокет

    @Nullable
    private Server.ServerCallback mCallback = null;

    private ServerLogicCallback mActionCallback = new ServerLogicCallback() {
        @Override
        public void alarm() {
            send(ALARM);
        }

        @Override
        public void action(Time time) {
            send(time.getTime());
        }
    };

    interface ServerLogicCallback {
        void alarm();
        void action(Time time);
    }

    public ServerLogic(Socket socket) throws IOException {
        Logger.d(TAG, "new ServerLogic");
        mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        mOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    public void setCallback(Server.ServerCallback callback) {
        mCallback = callback;
    }

    public ServerLogicCallback getActionCallback() {
        return mActionCallback;
    }

    @Override
    public void run() {
        String word;
        try {
            while (true) {
                word = mIn.readLine();
                Logger.d(TAG, "Get " + word);
                if(word.equals("stop")) {
                    break;
                }
                if (mCallback != null) {
                    if (word.contains(":")) {
                        Time alarm = new Time(word.split(" ")[1]);
                        Logger.d(TAG, "Set new alarm is " + alarm.getTime());
                        mCallback.addAlarm(alarm);
                        for (ServerLogic vr : Server.serverList) {
                            vr.send("Add new alarm " + alarm.getTime());
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.d(TAG, "Same IOException");
        }
    }

    private void send(String msg) {
        try {
            mOut.write(msg + "\n");
            mOut.flush();
        } catch (IOException ignored) {}
    }
}
