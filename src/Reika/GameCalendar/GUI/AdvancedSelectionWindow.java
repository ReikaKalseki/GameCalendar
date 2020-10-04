package Reika.GameCalendar.GUI;

import java.io.IOException;

import Reika.GameCalendar.Main;

import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AdvancedSelectionWindow implements EventHandler<javafx.event.Event> {

	private Stage window;
	private Scene display;
	private Parent root;
	private AdvancedSelectionController controller;

	//private static final GuiStateCache<AdvancedSelectionController> stateCache = new GuiStateCache("GuiState/Selection");

	public AdvancedSelectionWindow() {

	}

	public void init() throws IOException {
		window = new Stage();
		System.out.println("Initializing GUI.");
		window.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		window.setHeight(640);
		window.setResizable(false);

		FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/SelectionDialog.fxml"));
		root = loader.load();
		controller = loader.getController();

		display = new Scene(root);

		display.setFill(Color.rgb(0x22, 0xaa, 0xff));

		Rectangle2D resolution = Screen.getPrimary().getVisualBounds();

		window.setTitle("Advanced Selection Options");
		window.setScene(display);
		window.show();

		window.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				/*
				try {
					stateCache.save(controller);
				}
				catch (IOException e) {
					System.err.println("Could not save GUI state:");
					e.printStackTrace();
				}*/
				window.close();
			}
		});
	}

	@Override
	public void handle(javafx.event.Event event) {
		controller.update(null);
	}

	public void postInit(HostServices host) {
		controller.postInit(host);
		/*
		try {
			stateCache.load(controller);
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/
		controller.window = window;
	}

}
