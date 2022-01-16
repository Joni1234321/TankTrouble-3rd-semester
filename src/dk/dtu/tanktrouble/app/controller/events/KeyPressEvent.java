package dk.dtu.tanktrouble.app.controller.events;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.Player;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

public class KeyPressEvent {
    boolean isPressed = false;
    final KeyCodeCombination keyCombination;

    public KeyPressEvent (KeyCode code) {
        keyCombination = new KeyCodeCombination(code);
    }

    public void pressKey (KeyEvent event) {
        if (!isPressed && keyCombination.match(event)) {
            isPressed = true;
            try {
                Player player = Networking.getCurrentPlayer();
                if (player != null) {
                    player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.KEY_EVENT, "+"+keyCombination.getName());
                }
            } catch (InterruptedException ignored) {}
        }
    }

    public void releaseKey (KeyEvent event) {
        if (isPressed && keyCombination.match(event)) {
            isPressed = false;
            try {
                Player player = Networking.getCurrentPlayer();
                if (player != null) {
                    player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.KEY_EVENT, "-"+keyCombination.getName());
                }
            } catch (InterruptedException ignored) {}
        }
    }
}