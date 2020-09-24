package Reika.GameCalendar.VideoExport;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.ControllerBase;
import Reika.GameCalendar.Util.MathHelper;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class VideoGuiController extends ControllerBase {

	@FXML
	VBox root;

	@FXML
	private RadioButton jcodec;

	@FXML
	private RadioButton ffmpeg;

	@FXML
	private TextField mpegPath;

	@FXML
	private CheckBox pauseNew;

	@FXML
	private Label pauseLenText;

	@FXML
	private Slider pauseSlider;

	@FXML
	private Label speedText;

	@FXML
	private Slider speedSlider;

	@FXML
	private Button goButton;

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
	}

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);

		pauseSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(pauseSlider, newVal.doubleValue()));
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedSlider, newVal.doubleValue()));
	}

	private void updateSlider(Slider s, double val) {
		double step = s.getMajorTickUnit()/(s.getMinorTickCount()+1);
		double rounded = MathHelper.roundToNearestFraction(val, step);
		s.setValue(rounded);
		switch(this.getNode(s).fxID) {
			case "speedSlider":
				speedText.setText(rounded+" seconds");
				break;
			case "pauseSlider":
				break;
		}
	}

	@Override
	protected void onButtonClick(Object o, String id) {
		VideoRenderer.instance.startRendering(Main.getCalendarRenderer());
	}

	@Override
	protected void update(String fxID) {

	}

}
