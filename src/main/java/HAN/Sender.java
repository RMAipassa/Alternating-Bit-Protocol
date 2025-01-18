package HAN;

import java.net.*;
import java.io.*;

public class Sender {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Assuming both are on the same device
        int serverPort = 12345;
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            String broadcastMessage = "Who is the receiver?";
            byte[] sendData = broadcastMessage.getBytes();

            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket broadcastPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, serverPort);
            socket.send(broadcastPacket);
            System.out.println("Broadcast message sent... waiting for response");

            byte[] receiveData = new byte[256];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received response: " + response);

            String receiverIp = response.split(":")[1].trim();
            System.out.println("Connecting to receiver at " + receiverIp);

            Socket connectionSocket = new Socket(receiverIp, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream());

            String message;
            int bit = 0;  // Initial alternating bit

            while (true) {
                System.out.println("Enter a message: ");
                message = reader.readLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                String[] words = message.split(" ");

                for (String word : words) {
                    boolean acknowledged = false;
                    while (!acknowledged) {
                        outToServer.writeUTF(bit + word);
                        System.out.println("Sent: " + bit + word);

                        long startTime = System.currentTimeMillis();
                        boolean correctAck = false;

                        while (System.currentTimeMillis() - startTime < 3000) {
                            if (inFromServer.available() > 0) {
                                String ack = inFromServer.readUTF();
                                if (ack.equals(String.valueOf(bit))) {
                                    acknowledged = true;
                                    correctAck = true;
                                    System.out.println("Acknowledgment received for bit " + bit);
                                    bit = 1 - bit;  
                                    break;
                                } else {
                                    System.out.println("Incorrect acknowledgment");
                                    break;
                                }
                            }
                        }

                        if (!correctAck) {
                            System.out.println("Timeout or incorrect acknowledgment. Resending message...");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        }
    }
}
