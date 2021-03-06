package Reika.GameCalendar.GUI;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Util.DateStamp;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdvancedSelectionController extends ControllerBase {

	@FXML
	VBox root;

	@FXML
	private Button doSelect;

	@FXML
	private TextField date;

	@FXML
	private CheckBox anyYearDate;

	@FXML
	private ChoiceBox<String> selYear;
	@FXML
	private ChoiceBox<String> catYear;
	@FXML
	private ChoiceBox<String> memYear;

	@FXML
	private ChoiceBox<String> category;

	@FXML
	private TextField propertyKey;
	@FXML
	private TextField propertyValue;

	Stage window;

	private boolean initialized = false;

	private ToggleGroup sectionSelection;

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
	}

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);

		sectionSelection = new ToggleGroup();

		catYear.getItems().add("Any");
		memYear.getItems().add("Any");
		for (int y : Main.getCalendarRenderer().getYears()) {
			String s = String.valueOf(y);
			selYear.getItems().add(s);
			catYear.getItems().add(s);
			memYear.getItems().add(s);
		}

		catYear.getSelectionModel().select("Any");
		memYear.getSelectionModel().select("Any");

		for (String a : ActivityCategory.getSortedNameList(JFXWindow.getSortingMode())) {
			category.getItems().add(a);
		}

		for (Node n : root.getChildren()) {
			if (n instanceof TitledPane) {
				TitledPane ttl = (TitledPane)n;
				RadioButton b = new RadioButton();
				b.setToggleGroup(sectionSelection);
				ttl.setGraphic(b);
				ttl.setOnMouseClicked(e -> {
					if (e.getY() < 25) {
						b.selectedProperty().set(true);
						this.update("");
					}
				});
				b.selectedProperty().addListener(e -> this.update(""));
			}
		}

		initialized = true;
	}

	@Override
	protected void onButtonClick(Object o, String id) {
		switch(id) {
			case "doSelect":
				this.getActiveSelectionMode().select(this);
		}
		window.close();
	}

	private Selections getActiveSelectionMode() {
		return Selections.list[sectionSelection.getToggles().indexOf(sectionSelection.getSelectedToggle())];
	}

	@Override
	protected void update(String fxID) {
		switch(fxID) {

		}

		doSelect.disableProperty().set(sectionSelection.getSelectedToggle() == null);
	}

	private static enum Selections {
		YEAR,
		DATE,
		CATEGORY,
		MEMORABLE,
		PROPERTY,
		;

		private static final Selections[] list = values();

		public void select(AdvancedSelectionController con) {
			switch(this) {
				case YEAR:
					int year = Integer.parseInt(con.selYear.getSelectionModel().getSelectedItem());
					Main.getCalendarRenderer().selectComplexSub(item -> item.containsYear(year));
					break;
				case DATE:
					DateStamp date = DateStamp.parse(con.date.getText());
					Main.getCalendarRenderer().selectAllAtDate(date, con.anyYearDate.isSelected());
					break;
				case CATEGORY:
					String catsel = con.category.getSelectionModel().getSelectedItem();
					String catselYear = con.catYear.getSelectionModel().getSelectedItem();
					int catyear = catselYear.equals("Any") ? -1 : Integer.parseInt(catselYear);
					Main.getCalendarRenderer().selectComplexSub(item -> (catyear == -1 || item.containsYear(catyear)) && item.category == ActivityCategory.getByName(catsel));
					break;
				case MEMORABLE:
					String memselYear = con.memYear.getSelectionModel().getSelectedItem();
					int memyear = memselYear.equals("Any") ? -1 : Integer.parseInt(memselYear);
					Main.getCalendarRenderer().selectComplexSub(item -> item.isMemorable() && (memyear == -1 || item.containsYear(memyear)));
					break;
				case PROPERTY:
					String propkey = con.propertyKey.getText();
					String value = con.propertyValue.getText();
					if (Strings.isNullOrEmpty(value) || value.equals("<Any>"))
						value = null;
					final String valf = value;
					Main.getCalendarRenderer().selectComplexSub(item -> {
						String prop = item.getProperty(propkey);
						return prop != null && (valf == null || valf.equals(prop));
					});
					break;
				default:
					break;
			}
		}
	}

}
