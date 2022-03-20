package com.alexiv.finish.server;

import com.alexiv.finish.time.Alarm;
import com.alexiv.finish.time.AlarmCallback;
import com.alexiv.finish.time.Time;
import com.alexiv.finish.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import static com.alexiv.finish.utils.Constants.*;

public class Server {
    private static final String TAG = Server.class.getSimpleName();

    private final Alarm mMyTimer = new Alarm();

    public static LinkedList<ServerLogic> serverList = new LinkedList<>();

    interface ServerCallback {
        void parseMsg(String msg);
    }

    private final ServerCallback mCallback = msg -> {
        Logger.d(TAG, "ServerCallback: " + msg);
        if (msg == null) {
            Logger.d(TAG, "ServerCallback: msg is null");
            return;
        }
        String arg = msg.substring(0, 1);
        String text = msg.substring(2);
        switch (arg) {
            case SOCKET_ARG_ALARM:
                sendAndSetLogs("Set new alarm " + text);
                String time = text.substring(0, text.indexOf(" "));
                String alarmText = text.substring(text.indexOf(" ") + 1);
                mMyTimer.addAlarm(new Time(time), alarmText);
                break;
            case SOCKET_ARG_CONNECT:
                sendAndSetLogs("New connection " + text);
                break;
            case SOCKET_ARG_DISCONNECT:
                sendAndSetLogs("Disconnection " + text);
                break;
            case SOCKET_ARG_LOG:
            case SOCKET_ARG_TIME:
            case SOCKET_ARG_MSG:
                // no op
                break;
            default:
                Logger.d(TAG, "parseMsg: other arg: msg = " + msg);
                break;
        }
    };

    private final ServerUI.ServerUICallback mUICallback;
    private final AlarmCallback mTimerCallback = new AlarmCallback() {
        @Override
        public void action(Time time) {
            mUICallback.updateTimer(time);
            sendAndSetLogs(time.getTime());
            for(ServerLogic serverLogic : serverList) {
                serverLogic.sendTime(time.getTime());
            }
        }

        @Override
        public void startAction() {
            sendAndSetLogs("Timer is start");
        }

        @Override
        public void pauseAction() {
            sendAndSetLogs("Timer is pause");
        }

        @Override
        public void resumeAction() {
            sendAndSetLogs("Timer is resume");
        }

        @Override
        public void stopAction() {
            sendAndSetLogs("Timer is stop");
            mUICallback.stopTimer();
        }

        @Override
        public void endAction() {
            sendAndSetLogs("Timer is end");
            mUICallback.endTimer();
            for(ServerLogic serverLogic : serverList) {
                serverLogic.sendTime(TIME_IS_OVER);
            }
        }

        @Override
        public void alarm(Time time, String action) {
            sendAndSetLogs("Alarm " + time.getTime() + " " + action);
            for(ServerLogic serverLogic : serverList) {
                serverLogic.sendAlarm(time.getTime() + " " + action);
            }
        }
    };

    private void sendAndSetLogs(@Nullable String text) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String msg = "(" + time + ") " + text;
        mUICallback.addLog(msg);
        for(ServerLogic serverLogic : serverList) {
            serverLogic.sendLogs(msg);
        }
    }

    public Server(@NotNull ServerUI.ServerUICallback callback) {
        mUICallback = callback;
        mMyTimer.addAlarmCallback(mTimerCallback);
        Thread thread = new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(PORT);
                try {
                    while (true) { // Блокируется до возникновения нового соединения:
                        Socket socket = server.accept();
                        try {
                            ServerLogic sl = new ServerLogic(socket);
                            sl.setCallback(mCallback);
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

    @NotNull
    private final BufferedReader mIn; // поток чтения из сокета
    @NotNull
    private final BufferedWriter mOut; // поток записи в сокет

    @Nullable
    private Server.ServerCallback mCallback = null;

    public ServerLogic(@NotNull Socket socket) throws IOException {
        Logger.d(TAG, "new ServerLogic");
        mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        mOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String word;
        try {
            while (true) {
                word = mIn.readLine();
                Logger.d(TAG, "ServerLogic: " + word);
                if (mCallback != null) {
                    mCallback.parseMsg(word);
                }
            }
        } catch (IOException e) {
            Logger.d(TAG, "Same IOException");
        }
    }

    public void setCallback(Server.ServerCallback callback) {
        mCallback = callback;
    }

    public void sendTime(String text) {
        send(SOCKET_ARG_TIME + " " + text);
    }

    public void sendLogs(String text) {
        send(SOCKET_ARG_LOG + " " + text);
    }

    public void sendAlarm(String text) {
        send(SOCKET_ARG_ALARM + " " + text);
    }

    public void sendMsg(String msg) {
        send(SOCKET_ARG_MSG + " " + msg);
    }

    private void send(String msg) {
        try {
            mOut.write(msg + "\n");
            mOut.flush();
        } catch (IOException ignored) {}
    }
}
