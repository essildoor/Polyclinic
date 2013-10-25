package com.exigen.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerOld extends Thread {

    Socket s;
    int num;

    public static void main(String[] args) {
        try {
            int i = 0;
            ServerSocket serverSocket = new ServerSocket(3128, 0, InetAddress.getByName("localhost"));
            System.out.println("server started");

            while (true) {
                new ServerOld(i, serverSocket.accept());
                System.out.println("opening connection #" + ++i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerOld(int num, Socket s) {
        this.num = num;
        this.s = s;

        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            String data = "";
            while (!data.equals("stop")) {
                data = in.readLine();
                out.println("" + num + ": " + data);
            }
            s.close();
        } catch (IOException e) {
            System.out.println("init error: " + e);
        }
    }

}
