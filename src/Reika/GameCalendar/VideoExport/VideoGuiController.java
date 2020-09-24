package Reika.GameCalendar.VideoExport;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.ControllerBase;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.NonlinearSlider;
import Reika.GameCalendar.Util.ArrayHelper;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.MathHelper;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
	private TextField startDate;

	@FXML
	private TextField endDate;

	private ToggleGroup encoderOptions;

	Stage window;

	private boolean initialized = false;

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

		encoderOptions = new ToggleGroup();
		jcodec.setToggleGroup(encoderOptions);
		ffmpeg.setToggleGroup(encoderOptions);
		ffmpeg.setSelected(!Strings.isNullOrEmpty(VideoRenderer.instance.pathToFFMPEG));
		jcodec.setSelected(!ffmpeg.isSelected());
		pauseNew.setSelected(VideoRenderer.instance.pauseDuration > 0);

		speedSlider = (Slider)JFXWindow.replaceNode(this, speedSlider, new NonlinearSlider(speedSlider, daysPerFrameOptions, true));

		pauseSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(pauseSlider, newVal.doubleValue()));
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedSlider, newVal.doubleValue()));

		startDate.setText(Main.getTimeline().getStart().toString());
		endDate.setText(Main.getTimeline().getEnd().toString());

		this.updateSlider(pauseSlider, VideoRenderer.instance.pauseDuration);
		this.updateSlider(speedSlider, ArrayHelper.indexOf(daysPerFrameOptions, VideoRenderer.instance.daysPerFrame));

		if (VideoRenderer.instance.pathToFFMPEG != null)
			mpegPath.setText(VideoRenderer.instance.pathToFFMPEG);

		initialized = true;
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
		if (initialized)
			this.setSettings();
	}

	@Override
	protected void onButtonClick(Object o, String id) {
		this.setSettings();
		//"E:/My Documents/Programs and Utilities/ffmpeg-4.3.1-full_build/bin/ffmpeg.exe"
		VideoRenderer.instance.startRendering(Main.getCalendarRenderer());
		window.close();
	}

	private void setSettings() {
		String encoder = this.getNode((Node)encoderOptions.getSelectedToggle()).fxID;
		VideoRenderer.instance.pathToFFMPEG = encoder.equals("ffmpeg") ? mpegPath.getText() : null;
		VideoRenderer.instance.daysPerFrame = daysPerFrameOptions[(int)speedSlider.getValue()];
		VideoRenderer.instance.pauseDuration = pauseNew.isSelected() ? (int)pauseSlider.getValue() : 0;
		VideoRenderer.instance.startDate = DateStamp.parse(startDate.getText());
		VideoRenderer.instance.endDate = DateStamp.parse(endDate.getText());
	}

	@Override
	protected void update(String fxID) {
		switch(fxID) {

		}
		pauseSlider.setDisable(!pauseNew.isSelected());
		this.setSettings();
	}

}
