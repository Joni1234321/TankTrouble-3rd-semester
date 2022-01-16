package dk.dtu.tanktrouble.data;

import javafx.scene.input.KeyCode;
import org.jspace.Space;

import java.io.*;

public class Player implements Serializable {
     String id, name;
    public boolean isAdmin;
    public double hue;
    public Space channel;

    private Thread thread;

    public void setThread(Thread thread) {this.thread = thread;}

    public void forceStopThread(){
        thread.interrupt();
    }


    public boolean upPressed = false, rightPressed = false, downPressed = false, leftPressed = false, spacePressed = false;
    public void keyEvent (boolean newState, String key) {
        if (key.equals(KeyCode.UP.getName())) upPressed = newState;
        else if (key.equals(KeyCode.RIGHT.getName())) rightPressed = newState;
        else if (key.equals(KeyCode.DOWN.getName())) downPressed = newState;
        else if (key.equals(KeyCode.LEFT.getName())) leftPressed = newState;
        else if (key.equals(KeyCode.SPACE.getName()) && newState) spacePressed = true;
    }


    public Player(String id, String name, double hue, Space channel, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.hue = hue;
        this.channel = channel;
        this.isAdmin = isAdmin;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        id = in.readUTF();
        name = in.readUTF();
        isAdmin = in.readBoolean();
        hue = in.readDouble();
    }
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(name);
        out.writeBoolean(isAdmin);
        out.writeDouble(hue);
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Player: " +
                "id=" + id +
                ", name='" + name + '\'' + " is admin: " + isAdmin;
    }
}
