package Reika.GameCalendar.GUI;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Util.DateStamp;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdvancedSelectionController extends ControllerBase {

	@FXML
	VBox root;

	@FXML
	private Button selectToday;

	Stage window;

	private boolean initialized = false;

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
	}

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);



		initialized = true;
	}

	@Override
	protected void onButtonClick(Object o, String id) {
		switch(id) {
			case "selectToday":
				Main.getCalendarRenderer().selectAllAtDate(DateStamp.launch, true);
		}
		window.close();
	}

	@Override
	protected void update(String fxID) {
		switch(fxID) {

		}
	}

}
