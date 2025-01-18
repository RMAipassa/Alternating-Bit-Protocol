package HAN;

import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {
    static int serverPort = 12345;
    static int windowSize = 4;
    static int base = 0;
    static int nextSeqNum = 0;
    static String[] frames;
    static boolean[] acked = null;
    static Timer[] timers;
    static DatagramSocket socket = null;


    public static void main(String[] args) {
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
            PrintWriter outToServer = new PrintWriter(connectionSocket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            String message;

            while (true) {
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter a message: ");
                message = userInput.readLine();



                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                frames = message.split(" ");
                int n = frames.length;
                acked = new boolean[n];
                timers = new Timer[n];


                while (base < n) {
                    while (nextSeqNum < base + windowSize && nextSeqNum < n) {
                        sendFrame(outToServer, nextSeqNum, frames[nextSeqNum]);
                        startTimer(outToServer, nextSeqNum, frames[nextSeqNum]);
                        nextSeqNum++;
                    }

                    if (inFromServer.ready()) {
                        String ack = inFromServer.readLine();
                        int ackNum = Integer.parseInt(ack);
                        System.out.println("Acknowledgment received for frame: " + ackNum);
                        acked[ackNum] = true;

                        while (base < n && acked[base]) {
                            base++;
                        }
                    }
                }
            }
            System.out.println("All frames sent successfully");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        }
    }

    private static void startTimer(PrintWriter outToServer, int SeqNum, String frame)  {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if(!acked[SeqNum]) {
                        sendFrame(outToServer, SeqNum, frame);
                        startTimer(outToServer, SeqNum, frame);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 10000);
        timers[SeqNum] = timer;
    }

    private static void sendFrame(PrintWriter  outToServer, int SeqNum, String frame) throws IOException {
        System.out.println("Sending frame: " + SeqNum + " with message part: " + frame);
        outToServer.println(SeqNum + " " + frame);
    }
}
