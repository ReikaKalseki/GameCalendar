package Reika.GameCalendar.GUI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.fx.drift.DriftFXSurface;
import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.ActivityCategory.SortingMode;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Util.Colors;

import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GuiController implements EventHandler<ActionEvent> {

	@FXML
	private SplitPane root;

	@FXML
	private BorderPane renderField;

	@FXML
	private Pane calendarOverlay;

	@FXML TextArea descriptionPane;

	@FXML
	private ListView<String> catList;

	@FXML ListView<String> sortList;

	@FXML
	private CheckBox importantDates;

	@FXML
	private CheckBox highlights;

	@FXML
	private CheckBox currentDate;

	@FXML
	private CheckBox xmasBreak;

	@FXML
	private CheckBox readingWeek;

	@FXML
	private CheckBox summerBreak;

	@FXML
	private CheckBox memorable;

	@FXML
	private CheckBox selectHighlightsInSection;

	@FXML
	private CheckBox selectSectionsWithHighlight;

	@FXML
	private Button catAll;

	@FXML
	private Button catNone;

	@FXML
	private Button catFlip;

	@FXML
	private Button reloadFiles;

	@FXML
	private Button openFiles;

	@FXML
	private Button videoExport;

	@FXML
	private VBox imageContainer;

	@FXML
	private TitledPane screenshotsTitled;

	@FXML
	private VBox rightmostColumn;

	@FXML
	private ScrollPane imageScroller;

	@FXML
	Label status;

	DriftFXSurface renderer;

	DFXInputHandler mouseHandler;

	private HostServices host;

	private final HashMap<Object, NodeWrapper> allNodes = new HashMap();
	private final HashMap<String, NodeWrapper> optionNodes = new HashMap();
	private final HashMap<String, NodeWrapper> buttons = new HashMap();
	private final HashMap<String, NodeWrapper> listSelects = new HashMap();

	@FXML
	public void initialize() {
		this.addWrapperHooks(this.getAllNodes());
		catList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		sortList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		for (Node n : JFXWindow.getRecursiveChildren(root)) {
			if (n instanceof SplitPane) {
				SplitPane p = (SplitPane)n;
				//p.setMouseTransparent(true);
				for (Divider d : p.getDividers()) {
					d.positionProperty().addListener(new DividerLocker(d));
				}
				for (Node n2 : p.getChildrenUnmodifiable()) {
					if (n2 instanceof Region) {
						Region r = (Region)n2;
						double w = r.getWidth();
						double h = r.getHeight();
						r.setMaxWidth(w);
						r.setMinWidth(w);
						r.setMaxHeight(h);
						r.setMinHeight(h);
					}
				}
			}
		}
	}

	private static class CategoryListCell extends ListCell<String> {

		private CategoryListCell() {
			super();
			this.textProperty().bind(this.itemProperty());
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				this.setGraphic(null);
			}
			else {
				ActivityCategory cat = ActivityCategory.getByName(item);
				Image fxImage = this.generateCategoryColorBox(cat.color);
				ImageView imageView = new ImageView(fxImage);
				this.setGraphic(imageView);
				if (!Strings.isNullOrEmpty(cat.desc))
					this.setTooltip(new Tooltip(cat.desc));
			}
		}

		private Image generateCategoryColorBox(int color) {
			int s = 14;
			int t = 2;
			WritableImage img = new WritableImage(s, s);
			PixelWriter pw = img.getPixelWriter();
			Color c = Color.rgb(Colors.getRed(color), Colors.getGreen(color), Colors.getBlue(color));
			Color c2 = Color.rgb(0, 0, 0);
			for (int i = 0; i < s; i++) {
				for (int k = 0; k < s; k++) {
					pw.setColor(i, k, i < t || k < t || i >= s-t || k >= s-t ? c2 : c);
				}
			}
			return img;
		}
	}

	void postInit(HostServices host) {
		System.out.println("Post-initializing GUI.");
		this.host = host;
		renderer = new DriftFXSurface();
		mouseHandler = new DFXInputHandler(renderer);
		calendarOverlay.setOnMouseClicked(mouseHandler);
		calendarOverlay.setOnMouseMoved(mouseHandler);
		renderField.setCenter(renderer);
		renderField.setPadding(new Insets(0));

		Labelling.instance.init(calendarOverlay);

		//this.dynamicizeTextBoxes(root);
		sortList.setItems(FXCollections.observableList(SortingMode.list()));
		sortList.getSelectionModel().clearAndSelect(SortingMode.TIME.ordinal());
		catList.setItems(FXCollections.observableList(ActivityCategory.getSortedNameList(SortingMode.values()[sortList.getSelectionModel().getSelectedIndex()])));
		catList.getSelectionModel().selectAll();
		catList.setCellFactory(lv -> {
			ListCell<String> cell = new CategoryListCell();
			cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
				catList.requestFocus();
				if (!cell.isEmpty()) {
					int index = cell.getIndex();
					if (catList.getSelectionModel().getSelectedIndices().contains(index))
						catList.getSelectionModel().clearSelection(index);
					else
						catList.getSelectionModel().select(index);
					event.consume();
				}
				this.update(GuiElement.CATEGORIES.id);
			});
			return cell ;
		});

		for (NodeWrapper n : optionNodes.values()) {
			((CheckBox)n.object).selectedProperty().set(GuiElement.getByID(n.fxID).isDefaultChecked());
		}

		descriptionPane.setEditable(false);
		descriptionPane.wrapTextProperty().set(true);

		imageScroller.setFitToWidth(true);
		ScrollBar descScroll = (ScrollBar)descriptionPane.lookup(".scroll-bar:vertical");
		descScroll.setMinWidth(12);

		this.setImages(null);

		imageContainer.setPadding(Insets.EMPTY);
		screenshotsTitled.setPadding(Insets.EMPTY);

		//this.update();
	}

	/*
	private void dynamicizeTextBoxes(Parent p) {
		for (Node n : JFXWindow.getRecursiveChildren(p)) {
			if (n instanceof TextArea) {
				Node n2 = n.lookup(".text");
				if (n2 == null) {
					System.out.println("Node "+n+" has no text component?!");
					continue;
				}
				n2.boundsInLocalProperty().addListener((bounds, oldVal, newVal) -> {
					((TextArea)n).setPrefHeight(newVal.getHeight() + 10); // 10px vertical padding to prevent scrollbar from showing
				});
			}
		}
	}*/

	private void addWrapperHooks(Collection<NodeWrapper> li) {
		for (NodeWrapper n2 : li) {
			this.addHook(n2);
		}
	}

	private void addHook(NodeWrapper n2) {
		if (n2.object instanceof CheckBox) {
			((CheckBox)n2.object).setOnAction(this);
			optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ButtonBase) {
			((ButtonBase)n2.object).setOnAction(this);
			buttons.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ChoiceBox) {
			((ChoiceBox)n2.object).setOnAction(this);
			//optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ComboBox) {
			((ComboBox)n2.object).setOnAction(this);
			//optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof TextInputControl) {
			//((TextInputControl)n2.object).textProperty().addListener(this);
			//optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ListView) {
			((ListView)n2.object).getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
				@Override
				public void changed(ObservableValue observable, Object oldValue, Object newValue) {
					GuiController.this.update(n2.fxID);
				}

			});
			listSelects.put(n2.fxID, n2);
		}
	}

	private void collectAllNodes() throws Exception {
		allNodes.clear();
		Field[] fd = this.getClass().getDeclaredFields();
		for (Field f : fd) {
			if (f.getAnnotation(Deprecated.class) != null)
				continue;
			if (f.getAnnotation(FXML.class) == null)
				continue;
			Object o = f.get(this);
			if (o instanceof Node) {
				NodeWrapper nw = new NodeWrapper(f.getName(), (Node)o);
				allNodes.put(o, nw);
			}
		}
	}

	Collection<NodeWrapper> getAllNodes() {
		if (allNodes.isEmpty()) {
			try {
				this.collectAllNodes();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Collections.unmodifiableCollection(allNodes.values());
	}

	Collection<NodeWrapper> getAllActiveNodes() {
		Collection<NodeWrapper> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (!n.object.isDisabled())
				ret.add(n);
		}
		return ret;
	}

	public Collection<Parent> getSections() {
		Collection<Parent> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof Parent)
				ret.add((Parent)n.object);
		}
		return ret;
	}

	Node getOption(GuiElement e) {
		return optionNodes.get(e.id).object;
	}

	ListView getListView(GuiElement e) {
		return (ListView)listSelects.get(e.id).object;
	}

	Collection<TitledPane> getCollapsibleSections() {
		Collection<TitledPane> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof TitledPane)
				ret.add((TitledPane)n.object);
		}
		return ret;
	}

	public void handle(ActionEvent event) {

		Object o = event.getSource();
		String id = allNodes.get(o).fxID;
		this.update(id);

		if (o instanceof Button) {
			GuiElement.getByID(id).onButtonClick(this);
		}
	}

	void update(String fxID) {
		GuiElement gui = fxID != null ? GuiElement.getByID(fxID) : null;
		if (gui == null || gui.reloadCategoriesOnClick()) {
			List<String> e = catList.getSelectionModel().getSelectedItems();
			//catList.setItems(FXCollections.observableList(ActivityCategory.getSortedNameList(SortingMode.values()[sortList.getSelectionModel().getSelectedIndex()])));
			//catList.getSelectionModel().clearSelection();
			if (e != null && !e.isEmpty()) {

			}
		}
		if (gui != null && gui.reloadTexts() && !gui.resetRenderer()) {
			StatusHandler.postStatus("Reloading descriptions", 200);
			Main.getCalendarRenderer().calculateDescriptions();
		}
		else if (gui == null || gui.resetRenderer()) {
			StatusHandler.postStatus("Reloading render state", 200);
			Main.getCalendarRenderer().clearSelection();
			Labelling.instance.init(calendarOverlay);
			this.setImages(null);
		}
	}

	static class NodeWrapper {

		private final String fxID;
		private final Node object;

		private NodeWrapper(String id, Node o) {
			fxID = id;
			object = o;
		}

	}

	private static class DividerLocker implements ChangeListener<Number> {

		private final Divider split;
		private final double position;

		public DividerLocker(Divider d) {
			split = d;
			position = d.getPosition();
		}

		@Override
		public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
			split.setPosition(position);
		}

	}

	void setImages(List<? extends CalendarEvent> images) {
		imageContainer.getChildren().clear();
		if (images != null) {
			for (CalendarEvent e : images) {
				Image img = e.getScreenshot();
				if (img == null) {
					continue;
					/*
					img = new WritableImage(320, 180);
					PixelWriter pw = ((WritableImage)img).getPixelWriter();
					Color c = Color.rgb(240, 240, 240);
					Color c2 = Color.rgb(220, 220, 220);
					for (int i = 0; i < img.getWidth(); i++) {
						for (int k = 0; k < img.getHeight(); k++) {
							pw.setColor(i, k, Math.abs(i) == Math.abs(k) ? c2 : c);
						}
					}
					 */
				}
				ImageView v = new ImageView(img);
				v.setFitWidth(320-2);
				v.setFitHeight(180-1);
				//Tooltip.install(v, new Tooltip(e.name));
				TitledPane ttl = new TitledPane();
				ttl.setText(e.category.name+": "+e.name);
				ttl.setContent(v);
				ttl.setExpanded(true);
				ttl.setAnimated(false);
				ttl.setCollapsible(false);
				imageContainer.getChildren().add(ttl);
				imageContainer.setMargin(ttl, new Insets(4, 0, 0, 0));
				Tooltip.install(v, new Tooltip("Click to open:\n\n"+e.getScreenshotFile().getAbsolutePath()));
				v.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (false) {
							try {
								Runtime.getRuntime().exec("explorer.exe /select," + e.getScreenshotFile().getAbsolutePath());
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						}
						else {
							host.showDocument(e.getScreenshotFile().getAbsolutePath());
						}
					}
				});
				ttl.toFront();
			}
		}
		boolean flag = !imageContainer.getChildren().isEmpty();
		screenshotsTitled.setExpanded(flag);
		screenshotsTitled.disableProperty().set(!flag);
	}

	public static enum GuiElement {

		SELALL("catAll"),
		SELNONE("catNone"),
		INVERTSEL("catFlip"),
		RELOAD("reloadFiles"),
		OPENFILE("openFiles"),
		MAKEVIDEO("videoExport"),
		HOLIDAYS("importantDates"),
		HIGHLIGHTS("highlights"),
		TODAY("currentDate"),
		XMAS("xmasBreak"),
		READING("readingWeek"),
		SUMMER("summerBreak"),
		MEMORABLE("memorable"),
		HIGHLIGHTSINSECTION("selectHighlightsInSection"),
		SECTIONSWITHHIGHLIGHT("selectSectionsWithHighlight"),
		CATEGORIES("catList"),
		SORTORDER("sortList"),
		;

		private final String id;

		private static final HashMap<String, GuiElement> guiMap = new HashMap();

		private GuiElement(String s) {
			id = s;
		}

		private boolean isDefaultChecked() {
			switch(this) {
				case HIGHLIGHTS:
				case TODAY:
				case MEMORABLE:
				case HIGHLIGHTSINSECTION:
					return true;
				default:
					return false;
			}
		}

		public boolean reloadCategoriesOnClick() {
			switch(this) {
				case SORTORDER:
				case SELALL:
				case SELNONE:
				case RELOAD:
					return true;
				default:
					return false;
			}
		}

		public boolean resetRenderer() {
			switch(this) {
				case CATEGORIES:
				case SELALL:
				case SELNONE:
				case RELOAD:
				case HIGHLIGHTS:
					return true;
				default:
					return false;
			}
		}

		public boolean reloadTexts() {
			switch(this) {
				case SORTORDER:
					return true;
				default:
					return this.resetRenderer();
			}
		}

		private static GuiElement getByID(String fxID) {
			return guiMap.get(fxID);
		}

		public boolean isChecked() {
			return JFXWindow.getGUI().getCheckbox(this);
		}

		public boolean isStringSelected(String s) {
			return JFXWindow.getGUI().isListEntrySelected(this, s);
		}

		private void onButtonClick(GuiController c) {
			switch(this) {
				case SELNONE:
					c.catList.getSelectionModel().clearSelection();
					break;
				case SELALL:
					c.catList.getSelectionModel().selectAll();
					break;
				case INVERTSEL:
					HashSet<Integer> li = new HashSet(c.catList.getSelectionModel().getSelectedIndices());
					c.catList.getSelectionModel().clearSelection();
					List<String> li2 = c.catList.getItems();
					for (int i = 0; i < li2.size(); i++) {
						if (!li.contains(i))
							c.catList.getSelectionModel().select(i);
					}
					break;
				case RELOAD:
					break;
				case OPENFILE:
					Main.getCalendarRenderer().openSelectedFiles(c.host);
					break;
				case MAKEVIDEO:
					break;
				default:
					break;
			}
		}

		static {
			for (GuiElement e : values()) {
				guiMap.put(e.id, e);
			}
		}

	}

}
