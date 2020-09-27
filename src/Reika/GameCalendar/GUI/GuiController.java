package Reika.GameCalendar.GUI;

import java.io.IOException;
import java.util.ArrayList;
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
import Reika.GameCalendar.VideoExport.VideoOptionsWindow;

import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GuiController extends ControllerBase {

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
	private CheckBox mergeArcs;

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
	private VBox optionsContainer;

	@FXML
	private VBox imageContainer;

	@FXML
	private TitledPane screenshotsTitled;

	@FXML
	private VBox rightmostColumn;

	@FXML
	private ScrollPane imageScroller;

	@FXML
	private Slider privacy;

	@FXML
	Label status;

	DriftFXSurface renderer;

	DFXInputHandler mouseHandler;

	private boolean reloadCategories = true;

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
		catList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		sortList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);
		System.out.println("Post-initializing GUI.");
		renderer = new DriftFXSurface();
		mouseHandler = new DFXInputHandler(renderer);
		calendarOverlay.setOnMouseClicked(mouseHandler);
		calendarOverlay.setOnMouseMoved(mouseHandler);
		AnchorPane ap = (AnchorPane)renderField.getCenter();
		ap.getChildren().setAll(renderer);
		renderer.setPrefSize(800, 800);
		renderField.setPadding(new Insets(0));

		privacy.setMax(Main.getTimeline().getMaxPrivacyLevel());
		privacy.setValue(privacy.getMax());
		privacy.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				privacy.setValue((int)Math.round(newValue.doubleValue()));
				GuiController.this.update(GuiElement.PRIVACY.id);
			}
		});

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

		for (NodeWrapper n : this.getOptionNodes()) {
			if (n.object instanceof CheckBox)
				((CheckBox)n.object).selectedProperty().set(GuiElement.getByID(n.fxID).isDefaultChecked());
		}

		descriptionPane.setEditable(false);
		descriptionPane.wrapTextProperty().set(true);

		imageScroller.setFitToWidth(true);
		ScrollBar descScroll = (ScrollBar)descriptionPane.lookup(".scroll-bar:vertical");
		descScroll.setMinWidth(12);

		this.setImages(null);

		rightmostColumn.setFillWidth(true);

		imageContainer.setPadding(Insets.EMPTY);
		screenshotsTitled.setPadding(Insets.EMPTY);

		for (Node n : optionsContainer.getChildrenUnmodifiable()) {
			if (n instanceof Separator) {
				/*
				Region line = (Region)n.lookup(".line");
				line.setPadding(Insets.EMPTY);
				((Separator)n).setPadding(Insets.EMPTY);
				 */
				((Separator)n).setValignment(VPos.CENTER);
			}
		}
		optionsContainer.setSpacing(8);

		//this.update();
	}

	Node getOption(GuiElement e) {
		return this.getOption(e.id);
	}

	ListView getListView(GuiElement e) {
		return this.getListView(e.id);
	}

	@Override
	protected void update(String fxID) {
		GuiElement gui = fxID != null ? GuiElement.getByID(fxID) : null;
		if (gui == GuiElement.SORTORDER) {
			Main.getCalendarRenderer().preserveSelection();
		}
		if (reloadCategories && (gui == null || gui.reloadCategoriesOnClick())) {
			reloadCategories = false;
			ArrayList<String> e = new ArrayList(catList.getSelectionModel().getSelectedItems());
			catList.setItems(FXCollections.observableList(ActivityCategory.getSortedNameList(SortingMode.values()[sortList.getSelectionModel().getSelectedIndex()])));
			catList.getSelectionModel().clearSelection();
			for (String s : e) {
				catList.getSelectionModel().select(s);
			}
			reloadCategories = true;
		}
		if (gui != null && gui.reloadTexts() && !gui.resetRenderer()) {
			StatusHandler.postStatus("Reloading descriptions", 200);
			Main.getCalendarRenderer().calculateDescriptions();
		}
		else if (gui == null || gui.resetRenderer()) {
			StatusHandler.postStatus("Reloading render state", 200);
			Main.getCalendarRenderer().clearSelection();
			//Labelling.instance.init(calendarOverlay);
			this.setImages(null);
		}
		if (gui == GuiElement.SORTORDER) {
			Main.getCalendarRenderer().restoreSelection();
		}
		videoExport.disableProperty().set(!this.isVideoExportValid());
		openFiles.disableProperty().set(GuiElement.ARCMERGE.isChecked());
	}

	private boolean isVideoExportValid() {
		return !GuiElement.ARCMERGE.isChecked();// && this.areAllCategoriesActive();
	}

	private boolean areAllCategoriesActive() {
		for (String s : ActivityCategory.getNameList()) {
			if (!GuiElement.CATEGORIES.isStringSelected(s))
				return false;
		}
		return true;
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
							GuiController.this.getHost().showDocument(e.getScreenshotFile().getAbsolutePath());
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

	@Override
	protected void onButtonClick(Object o, String id) {
		if (o instanceof Button) {
			GuiElement.getByID(id).onButtonClick(this);
		}
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
		ARCMERGE("mergeArcs"),
		HIGHLIGHTSINSECTION("selectHighlightsInSection"),
		SECTIONSWITHHIGHLIGHT("selectSectionsWithHighlight"),
		CATEGORIES("catList"),
		SORTORDER("sortList"),
		PRIVACY("privacy"),
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
				case ARCMERGE:
				case PRIVACY:
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

		public double getValue() {
			return JFXWindow.getGUI().getSliderValue(this);
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
					StatusHandler.postStatus("Loading files", 500, false);
					Main.load();
					break;
				case OPENFILE:
					Main.getCalendarRenderer().openSelectedFiles(c.getHost());
					break;
				case MAKEVIDEO:
					try {
						VideoOptionsWindow vow = new VideoOptionsWindow();
						vow.init();
						vow.postInit(JFXWindow.getGUI().getHostServices());
					}
					catch (IOException e) {
						StatusHandler.postStatus("Failed to load video window", 4000);
						e.printStackTrace();
					}
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
