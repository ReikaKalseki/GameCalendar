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
import javafx.util.StringConverter;

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

	private final int[] daysPerFrameOptions = {-5, -2, 1, 2, 3, 4, 5, 6, 7, 15, 30};

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
	}

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);

		speedSlider.setMajorTickUnit(1);
		speedSlider.setMinorTickCount(0);
		speedSlider.setMin(0);
		speedSlider.setMax(daysPerFrameOptions.length-1);

		pauseSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(pauseSlider, newVal.doubleValue()));
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedSlider, newVal.doubleValue()));
		speedSlider.setLabelFormatter(new StringConverter<Double>() {

			@Override
			public String toString(Double val) {
				int index = (int)Math.round(val);
				int sp = daysPerFrameOptions[index];
				return sp > 0 ? String.valueOf(sp) : "1/"+(-sp);
			}

			@Override
			public Double fromString(String s) {
				return Double.parseDouble(s);
			}

		});
	}

	private void updateSlider(Slider s, double val) {
		double step = s.getMajorTickUnit()/(s.getMinorTickCount()+1);
		double rounded = MathHelper.roundToNearestFraction(val, step);
		s.setValue(rounded);
		switch(this.getNode(s).fxID) {
			case "pauseSlider":
				pauseLenText.setText(rounded+" seconds");
				break;
			case "speedSlider":
				rounded = daysPerFrameOptions[(int)rounded];
				speedText.setText(rounded+" days per frame");
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
