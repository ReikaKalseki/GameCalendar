package Reika.GameCalendar.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.fx.drift.DriftFXSurface;

import Reika.GameCalendar.Main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

@Deprecated
public class Window extends Application implements EventHandler<javafx.event.Event> {

	private static Window gui;

	private Stage window;
	private Scene display;
	private Parent root;
	private GuiController controller;

	public Window() {
		gui = this;
	}

	@Override
	public void start(Stage primary) throws Exception {
		this.init(primary);
	}

	private void init(Stage primary) throws IOException {
		window = primary;
		window.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));

		FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/GUI.fxml"));
		root = loader.load();
		controller = loader.getController();

		display = new Scene(root);

		display.setFill(Color.rgb(0x22, 0xaa, 0xff));

		Rectangle2D resolution = Screen.getPrimary().getVisualBounds();

		window.setTitle("Game Calendar - ReikaKalseki, 2020");
		window.setScene(display);
		//window.setWidth(resolution.getWidth()/2);
		//window.setHeight(resolution.getHeight()/2);
		//window.setX(resolution.getWidth()/4);
		//window.setY(resolution.getHeight()/4);
		window.show();
		controller.postInit();

		double midX = window.getWidth()/2;
		double midY = window.getHeight()/2;

		this.setStatus("The status line works in 2020.");
		for (TitledPane p : controller.getCollapsibleSections()) {
			p.setExpanded(false);
		}
	}

	public void updateActiveSections() {

	}

	public void setStatus(String value) {
		controller.status.setText(value);
	}

	public static void create() {
		launch();
	}

	public static Window getGUI() {
		return gui;
	}

	public static DriftFXSurface getRenderPane() {
		return (DriftFXSurface)gui.controller.renderer;
	}

	@Override
	public void handle(javafx.event.Event event) {
		controller.update();
	}

	public static ArrayList<Node> getRecursiveChildren(Parent root) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		addAllChildren(root, nodes);
		return nodes;
	}

	private static void addAllChildren(Parent p, ArrayList<Node> nodes) {
		for (Node n : getDirectChildren(p)) {
			nodes.add(n);
			if (n instanceof Parent)
				addAllChildren((Parent)n, nodes);
		}
	}

	private static Collection<Node> getDirectChildren(Parent p) {
		Collection<Node> ret = new ArrayList();
		ret.addAll(p.getChildrenUnmodifiable());
		if (p instanceof SplitPane) {
			ret.addAll(((SplitPane)p).getItems());
		}
		if (p instanceof Accordion) {
			ret.addAll(((Accordion)p).getPanes());
		}
		if (p instanceof TitledPane) {
			ret.add(((TitledPane)p).getContent());
		}
		return ret;
	}

}
