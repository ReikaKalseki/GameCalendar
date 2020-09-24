package Reika.GameCalendar.VideoExport;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.ControllerBase;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.NonlinearSlider;
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

	private final double[] daysPerFrameOptions = {0.2, 0.5, 1, 2, 3, 4, 5, 6, 7, 15, 30};

	@FXML
	@Override
	public void initialize() {
		this.preInit(root);
		super.initialize();
	}

	@Override
	protected void postInit(HostServices host) {
		super.postInit(host);

		speedSlider = (Slider)JFXWindow.replaceNode(this, speedSlider, new NonlinearSlider(speedSlider, daysPerFrameOptions, true));

		pauseSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(pauseSlider, newVal.doubleValue()));
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedSlider, newVal.doubleValue()));
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
				speedText.setText(MathHelper.fractionalize(daysPerFrameOptions[(int)rounded])+" days per frame");
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
