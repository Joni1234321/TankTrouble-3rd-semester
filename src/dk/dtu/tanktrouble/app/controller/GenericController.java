package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;

public abstract class GenericController {

	public interface ControlResponder {
		void control(Object controllerInstance);
	}

	public void initializeScene(Main.SceneInitializer initializer, Scene scene) {
		initializer.initializeScene(getInstance(), scene);
	}

	private ControlResponder onControlResponder;

	public static final EventHandler<? super ActionEvent> defaultHandler = (event) -> {
	};

	public final void beginControl() {
		onControlResponder.control(getInstance());
		initializeController();
	}

	abstract Object getInstance();

	public void initializeController() {
	}

	public final void setOnControlResponder(ControlResponder responder) {
		this.onControlResponder = responder;
	}

}
