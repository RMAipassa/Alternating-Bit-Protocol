package HAN;

import java.net.*;
import java.io.*;

public class Receiver {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             DataInputStream inFromClient = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())) {

            int expectedBit = 0;
            while (true) {
                String received = inFromClient.readUTF();
                int receivedBit = Integer.parseInt(received.substring(0, 1));
                String message = received.substring(1);

                if (receivedBit == expectedBit) {
                    System.out.println("Received message: " + message + " with bit " + receivedBit);
                    outToClient.writeUTF(String.valueOf(receivedBit));
                    expectedBit = 1 - expectedBit;
                } else {
                    System.out.println("Received bit was not the expected bit. Is either a duplicate, an error or we missed a message");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

