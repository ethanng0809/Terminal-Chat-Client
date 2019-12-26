package assignment6;

import java.net.*;
import java.io.*;
import java.util.*;

public class MyClient {

    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    private Socket socket;
    private String server;
    private int port;

    private String username;

    public MyClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }
    // Open the input and output streams, start a listener to check for typing
    // Start the socket based on the given port
    // Return true if able to accomplish the above, if not return false
    public boolean start() {
        try {
            socket = new Socket(server, port);
            inStream  = new ObjectInputStream(socket.getInputStream());
            outStream = new ObjectOutputStream(socket.getOutputStream());
            new serverListener().start();
            outStream.writeObject(username);
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    // Start the client and get the username
    // Always listen for what is typed and send to server
    public static void main(String[] args) {
        int portNumber = 6666;
        String serverAddress = "localhost";
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String name = sc.nextLine();

        MyClient client = new MyClient(serverAddress, portNumber, name);
        if(!client.start())
            return;

        System.out.println("\nWelcome to the Chat Room.");
        System.out.println("Type the message by itself to send to all clients");
        System.out.println("Type @name then message to message specific client");
        System.out.println("Type online to see other clients");
        System.out.println("Type logoff to exit\n");

        while(true) {
            String msg = sc.nextLine();
            if(msg.equalsIgnoreCase("LOGOFF")) {
                client.sendMessage(new MyMessage(MyMessage.LOGOFF, ""));
                break;
            }
            else if(msg.equalsIgnoreCase("ONLINE")) {
                client.sendMessage(new MyMessage(MyMessage.ONLINE, ""));
            }
            else {
                client.sendMessage(new MyMessage(MyMessage.MESSAGE, msg));
            }
        }
        sc.close();
        client.disconnect();
    }

    private void display(String msg) { System.out.print(msg); }
    // Write a message to the output strteam
    private void sendMessage(MyMessage msg) {
        try {
            outStream.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }
    // Close all of the streams and the socket
    private void disconnect() {
        try {
            if(inStream != null) inStream.close();
            if(outStream != null) outStream.close();
            if(socket != null) socket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    // Class to listen for the server's output
    class serverListener extends Thread {
        public void run() {
            while(true) {
                try {
                    String msg = (String) inStream.readObject();
                    System.out.println(msg);
                }
                catch(Exception e) {
                    display("SYSTEM: Connection has been Terminated\n");
                    System.exit(1);
                }
            }
        }
    }
}