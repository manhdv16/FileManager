package com.ltm.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final int NUM_OF_THREAD = 10;
    public final static int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started with port: "+SERVER_PORT);
            System.out.println("Waiting for a client ...");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client accepted: " + socket);

                    ServerHandler handler = new ServerHandler(socket);
                    executor.execute(handler);
                } catch (IOException e) {
                    System.err.println(" Connection Error: " + e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

}
