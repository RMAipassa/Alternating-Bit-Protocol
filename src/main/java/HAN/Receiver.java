package HAN;

import java.net.*;
import java.io.*;

public class Receiver {
    public static void main(String[] args) {
        int port = 12345;

        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            System.out.println("Receiver is waiting for a broadcast...");

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            InetAddress senderAddress = packet.getAddress();
            System.out.println("Received broadcast from " + senderAddress.getHostAddress());

            String responseMessage = "Receiver IP: " + InetAddress.getLocalHost().getHostAddress();
            DatagramPacket responsePacket = new DatagramPacket(responseMessage.getBytes(), responseMessage.length(), senderAddress, packet.getPort());
            socket.send(responsePacket);
            System.out.println("Sent response back to the sender");

            ServerSocket serverSocket = new ServerSocket(port);
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Connection established with the sender.");

            DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());


            int expectedBit = 0;
            while (true) {
                String received = inFromClient.readUTF();
                int receivedBit = Integer.parseInt(received.substring(0, 1));
                String word = received.substring(1);
                if (receivedBit == expectedBit) {
                    System.out.println("Received word: '" + word + "' with bit " + receivedBit);
                    outToClient.writeUTF(String.valueOf(receivedBit));
                    expectedBit = 1 - expectedBit;
                } else {
                    System.out.println("Received bit was not the expected bit. Is either a duplicate, an error or we missed a message");
                    outToClient.writeUTF(String.valueOf(expectedBit));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

