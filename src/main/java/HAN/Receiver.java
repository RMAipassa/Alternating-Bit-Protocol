package HAN;

import java.net.*;
import java.io.*;

public class Receiver {
    public static void main(String[] args) {
        int port = 12345;
        int expectedNum = 0;

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

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);


            while (true) {
                String received = inFromClient.readLine();
                String[] messageParts = received.split(" ");
                int seqNum = Integer.parseInt(messageParts[0]);
                String message = messageParts[1];
                if(seqNum == expectedNum){
                    expectedNum++;
                    System.out.println("Received: Frame: " + seqNum + " with message part: " + message);
                    outToClient.println(seqNum);
                    System.out.println("sent ack: " + seqNum);
                } else {
                    System.out.println("Received: Frame: " + seqNum + " with message part: " + message + " - Discarded");
                    outToClient.println(expectedNum - 1);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

