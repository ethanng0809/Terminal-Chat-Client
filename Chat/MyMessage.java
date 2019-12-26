package assignment6;

import java.io.*;
// Class to store the message types

public class MyMessage implements Serializable {

    static final int MESSAGE = 0;
    static final int LOGOFF = 1;
    static final int ONLINE = 2;
    private int type;
    private String message;

    public MyMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }
    public int getType() { return type; }
    public String getMessage() { return message; }
}