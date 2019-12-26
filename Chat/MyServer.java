package assignment6;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyServer {

    private static int uniqueId;
    private ArrayList<serverWorker> threadList;
    private SimpleDateFormat format;
    private int port;
    private boolean status;

    public MyServer(int port) {
        this.port = port;
        format = new SimpleDateFormat("HH:mm:ss");
        threadList = new ArrayList<>();
    }
    // Hosts a server at the given port
    public static void main(String[] args) {
        int portNumber = 6666;
        MyServer server = new MyServer(portNumber);
        server.start();
    }
    // Starts the server and listens for clients to join
    public void start() {
        status = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(status) {
                display("Waiting for Connections on Port " + port + ".");
                Socket socket = serverSocket.accept();
                if(!status)
                    break;
                serverWorker t = new serverWorker(socket);
                threadList.add(t);
                t.start();
            }
            serverSocket.close();
            for(int i = 0; i < threadList.size(); ++i) {
                serverWorker tc = threadList.get(i);
                tc.inStream.close();
                tc.oStream.close();
                tc.socket.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Sends a message with the specified date
    private void display(String msg) { System.out.println(format.format(new Date()) + " " + msg); }
    // Send a message to different clients
    private synchronized boolean broadcast(String message) {
        String time = format.format(new Date());
        String[] w = message.split(" ",3);
        if(w[1].charAt(0) == '@') {
            String toCheck = w[1].substring(1);
            message = w[0] + w[2];
            String formatMessage = time + " " + message + "\n";
            boolean found = false;
            for(int i = 0; i < threadList.size(); i++) {
                serverWorker worker = threadList.get(i);
                String check = worker.getUsername();
                if(check.equals(toCheck)) {
                    if(!worker.writeMsg(formatMessage)) {
                        threadList.remove(i);
                        display("Client " + worker.username + " has been removed.");
                    }
                    found = true;
                    break;
                }
            }
            if(found != true)
                return false;
        }
        else {
            String send = time + " " + message + "\n";
            System.out.print(send);
            for(int i = 0; i < threadList.size(); i++) {
                serverWorker worker = threadList.get(i);
                if(!worker.writeMsg(send)) {
                    threadList.remove(i);
                    display("Client " + worker.username + " has been removed.");
                }
            }
        }
        return true;
    }
    // Removes a client from broadcast
    private synchronized void remove(int id) {
        String dc = "";
        for(int i = 0; i < threadList.size(); ++i) {
            serverWorker worker = threadList.get(i);
            if(worker.id == id) {
                dc = worker.getUsername();
                threadList.remove(i);
                break;
            }
        }
        broadcast("SYSTEM: " + dc + " has left the chat room.");
    }
    // Class to store a worker which handles the communication between client and server
    class serverWorker extends Thread {
        Socket socket;
        ObjectInputStream inStream;
        ObjectOutputStream oStream;
        int id;
        String username;
        MyMessage msg;
        String date;

        serverWorker(Socket socket) {
            id = uniqueId++;
            this.socket = socket;
            date = new Date().toString() + "\n";
            try {
                oStream = new ObjectOutputStream(socket.getOutputStream());
                inStream  = new ObjectInputStream(socket.getInputStream());
                username = (String) inStream.readObject();
                broadcast("SYSTEM: " + username + " has joined the chat room.");
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        public String getUsername() { return username; }
        // Handles the checking of messages between server and client
        public void run() {
            boolean status = true;
            while(status) {
                try {
                    msg = (MyMessage) inStream.readObject();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                String message = msg.getMessage();
                switch(msg.getType()) {
                    case 0:
                        boolean confirmation = broadcast(username + ": " + message);
                        if(confirmation == false){
                            String msg = "SYSTEM: User Does Not Exist";
                            writeMsg(msg);
                        }
                        break;
                    case 1:
                        display(username + " disconnected with a LOGOUT message.");
                        status = false;
                        break;
                    case 2:
                        writeMsg("List of the users connected at " + format.format(new Date()) + "\n");
                        for(int i = 0; i < threadList.size(); i++) {
                            serverWorker worker = threadList.get(i);
                            writeMsg((i+1) + ". " + worker.username + " at " + worker.date);
                        }
                        break;
                }
            }
            remove(id);
            close();
        }
        // Properly closes the started input streams
        private void close() {
            try {
                if (oStream != null) oStream.close();
                if (inStream != null) inStream.close();
                if (socket != null) socket.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        // Writes a message to the specified client(s)
        private boolean writeMsg(String msg) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                oStream.writeObject(msg);
            }
            catch(IOException e) {
                display("SYSTEM: Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}
