package com.alexiv.finish.client;

import com.alexiv.finish.server.Server;
import com.alexiv.finish.utils.Time;
import com.alexiv.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.alexiv.finish.utils.Constants.*;

public class Client {
    private static final String TAG = Client.class.getSimpleName();

    @NotNull
    private ClientUI.ClientUICallback mUICallback;
    @NotNull
    private ClientLogic clientLogic;

    interface ClientCallback {
        void getMessage(String msg);
        void exit();
    }

    private ClientCallback mCallback = new ClientCallback() {
        @Override
        public void getMessage(String msg) {
            Logger.d(TAG, "ClientCallback: " + msg);
            mUICallback.log(msg);
            // TODO

        }

        @Override
        public void exit() {
            mUICallback.exit();
        }
    };

    public Client(@NotNull ClientUI.ClientUICallback callback) {
        mUICallback = callback;
        clientLogic = new ClientLogic(IP_ADDRESS, PORT, mCallback);
    }

    public void setAlarm(@NotNull Time time) {
        Logger.d(TAG, "send alarm " + time.getTime());
        clientLogic.sendNewAlarm(time);
    }
}

class ClientLogic {
    private static final String TAG = ClientLogic.class.getSimpleName();

    private Socket mSocket;
    private BufferedReader mIn; // поток чтения из сокета
    private BufferedWriter mOut; // поток чтения в сокет
    private BufferedReader mInputUser; // поток чтения с консоли

    @NotNull
    private final String mId;

    @Nullable
    private Client.ClientCallback mCallback = null;

    public ClientLogic(String address, int port, Client.ClientCallback callback) {
        mId = new Object().toString().split("@")[1];
        setCallbackMsg(callback);
        try {
            mSocket = new Socket(address, port);
            mInputUser = new BufferedReader(new InputStreamReader(System.in));
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOut = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            setIdClient();
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            Logger.d(TAG, "Constructor failed");
            ClientLogic.this.downService();
        }
    }

    public void setCallbackMsg(Client.ClientCallback callback) {
        mCallback = callback;
    }

    public void sendNewAlarm(@NotNull Time time) {
        send(String.format(SEND_TIMER_FORMAT, mId, time.getTime()));
    }

    private void setIdClient() {
        send("Connect " + mId + "\n");
    }

    private void downService() {
        try {
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
                mIn.close();
                mOut.close();
            }
        } catch (IOException ignored) {
        }
        mCallback.exit();
    }

    private void send(@NotNull String text) {
        try {
            Logger.d(TAG, "send: " + text);
            mOut.write(text);
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
                    if (str.equals("stop")) {
                        ClientLogic.this.downService();
                        break;
                    }
                    mCallback.getMessage(str);
                }
            } catch (IOException e) {
                ClientLogic.this.downService();
            }
        }
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date()); // время
                    userWord = mInputUser.readLine(); // сообщения с консоли
                    if (userWord.equals("stop")) {
                        mOut.write("stop" + "\n");
                        ClientLogic.this.downService();
                        break;
                    } else {
                        mOut.write("(" + time + ") " + mId + ": " + userWord + "\n"); // отправляем на сервер
                    }
                    mOut.flush();
                } catch (IOException e) {
                    ClientLogic.this.downService();
                }
            }
        }
    }
}
