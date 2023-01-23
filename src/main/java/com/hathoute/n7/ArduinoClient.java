package com.hathoute.n7;

import com.hathoute.n7.utils.InputStreamWrapper;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ArduinoClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8181;


    public static void main(String[] args) throws Exception {
        var socket = new Socket(HOST, PORT);
        var oos = socket.getOutputStream();
        var ois = new InputStreamWrapper(socket.getInputStream());

        System.out.println("Just connected to " + socket.getRemoteSocketAddress());

        String msg = "gzsh";
        oos.write(msg.getBytes(StandardCharsets.US_ASCII));
        oos.write(0);
        oos.flush();

        //read the server response message
        String message = (String) ois.readString(10, false);

        System.out.println("Message: " + message);
        //close resources
        oos.close();
        ois.close();
        socket.close();//!!!!
    }
}
