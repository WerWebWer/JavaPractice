package com.alexiv.finish.client;

import com.alexiv.finish.time.Time;
import com.alexiv.finish.utils.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

import static com.alexiv.finish.utils.Constants.*;

public class Client {
    private static final String TAG = Client.class.getSimpleName();

    @NotNull
    private final String mId;
    @NotNull
    private final ClientUI.ClientUICallback mUICallback;

    private Socket mSocket;
    private BufferedReader mIn; // поток чтения из сокета
    private BufferedWriter mOut; // поток чтения в сокет

    public Client(@NotNull ClientUI.ClientUICallback callback) {
        Logger.d(TAG, "constructor");
        mUICallback = callback;
        mId = new Object().toString().split("@")[1];
        mUICallback.setId(mId);
        try {
            mSocket = new Socket(IP_ADDRESS, PORT);
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOut = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

            send(SOCKET_ARG_CONNECT + " " + mId);

            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
        } catch (IOException e) {
            Logger.d(TAG, "constructor failed");
            downService();
        }
    }

    public void setAlarm(@NotNull Time time) {
        Logger.d(TAG, "setAlarm: " + time.getTime());
        send(SOCKET_ARG_ALARM + " " + time.getTime());
    }

    private void parseMsg(String msg) {
        Logger.d(TAG, "parseMsg: " + msg);
        String arg = msg.substring(0, 1);
        String text = msg.substring(2);
        switch (arg) {
            case SOCKET_ARG_LOG:
                mUICallback.log(text);
                break;
            case SOCKET_ARG_ALARM:
                mUICallback.alarm();
                break;
            case SOCKET_ARG_TIME:
                mUICallback.time(new Time(text));
                break;
            case SOCKET_ARG_CONNECT:
            case SOCKET_ARG_MSG:
                // no op
                break;
            default:
                Logger.d(TAG, "parseMsg: other arg: msg = " + msg);
                break;
        }
    }

    private void downService() {
        Logger.d(TAG, "downService");
        try {
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
                mIn.close();
                mOut.close();
            }
        } catch (IOException ignored) {
            Logger.d(TAG, "downService: IOException");
        }
        mUICallback.exit();
    }

    private void send(@NotNull String text) {
        Logger.d(TAG, "send: " + text);
        try {
            mOut.write(text + "\n");
            mOut.flush();
        } catch (IOException ignored) {
            Logger.d(TAG, "send: IOException");
        }
    }

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    str = mIn.readLine();
                    Logger.d(TAG, "ReadMsg: " + str);
                    parseMsg(str);
                }
            } catch (IOException e) {
                downService();
            }
        }
    }
}
