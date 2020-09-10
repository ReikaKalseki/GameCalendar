package Reika.GameCalendar.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.fx.drift.DriftFXSurface;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.ActivityCategory.SortingMode;
import Reika.GameCalendar.Data.CalendarEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class JFXWindow extends Application implements EventHandler<javafx.event.Event> {

	private static JFXWindow gui;

	private static boolean isLoaded = false;

	private Stage window;
	private Scene display;
	private Parent root;
	private GuiController controller;

	public JFXWindow() {
		gui = this;
	}

	@Override
	public void start(Stage primary) throws Exception {
		this.init(primary);
	}

	private void init(Stage primary) throws IOException {
		System.out.println("Initializing GUI.");
		window = primary;
		window.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		window.setResizable(false);

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

		primary.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
			}
		});

		this.setStatus("Program initialized.");
		controller.postInit(this.getHostServices());

		double midX = window.getWidth()/2;
		double midY = window.getHeight()/2;

		for (TitledPane p : controller.getCollapsibleSections()) {
			p.setExpanded(false);
		}

		isLoaded = true;
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.out.println("Closing window");
	}

	public void updateActiveSections() {

	}

	void setStatus(String value) {
		controller.status.setText(value);
	}

	public void setScreenshots(List<? extends CalendarEvent> images) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.setImages(images);
			}
		});
	}

	public static void create() {
		launch();
	}

	public static JFXWindow getGUI() {
		return gui;
	}

	public static boolean isLoaded() {
		return isLoaded;
	}

	public static DriftFXSurface getRenderPane() {
		return gui != null && gui.controller != null ? gui.controller.renderer : null;
	}

	public static TextArea getDescriptionPane() {
		return gui != null && gui.controller != null ? gui.controller.descriptionPane : null;
	}

	public static DFXInputHandler getMouseHandler() {
		return gui != null && gui.controller != null ? gui.controller.mouseHandler : null;
	}

	public SortingMode getSortingMode() {
		return gui != null && gui.controller != null ? SortingMode.values()[gui.controller.sortList.getSelectionModel().getSelectedIndex()] : SortingMode.ALPHA;
	}

	public boolean getCheckbox(String id) {
		Node n = this.getOption(id);
		return n != null && ((CheckBox)n).selectedProperty().get();
	}

	public boolean isListEntrySelected(String node, String s) {
		ListView n = this.getListView(node);
		return n != null && n.getSelectionModel().isSelected(n.getItems().indexOf(s));
	}

	private Node getOption(String id) {
		return gui != null && gui.controller != null ? gui.controller.getOption(id) : null;
	}

	private ListView getListView(String id) {
		return gui != null && gui.controller != null ? gui.controller.getListView(id) : null;
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
