package HAN;

import java.net.*;
import java.io.*;

public class Sender {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Assuming both are on the same device
        int serverPort = 12345;

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
             DataInputStream inFromServer = new DataInputStream(socket.getInputStream())) {

            String message;
            int bit = 0;  // Initial alternating bit

            while (true) {
                System.out.println("Enter a message: ");
                message = reader.readLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Send message with alternating bit
                outToServer.writeUTF(bit + message);
                System.out.println("Sent: " + bit + " " +  message);

                // Wait for acknowledgment
                boolean acknowledged = false;
                while (!acknowledged) {
                    String ack = inFromServer.readUTF();
                    if (ack.equals(String.valueOf(bit))) {
                        acknowledged = true;
                        System.out.println("Acknowledgment received for bit " + bit);
                        bit = 1 - bit;  // Alternate the bit
                    } else {
                        System.out.println("Waiting for correct acknowledgment...");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
