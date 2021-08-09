package Reika.GameCalendar.VideoExport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.ControllerBase;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.NonlinearSlider;
import Reika.GameCalendar.Rendering.CalendarRenderer;
import Reika.GameCalendar.Rendering.RenderLoop;
import Reika.GameCalendar.Util.ArrayHelper;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.MathHelper;

import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
	private CheckBox copyToDFX;

	@FXML
	private Label pauseLenText;

	@FXML
	private Slider pauseSlider;

	@FXML
	private Label speedText;

	@FXML
	private Slider speedSlider;

	@FXML
	private CheckBox speedEmpty;

	@FXML
	private Label speedEmptyText;

	@FXML
	private Slider speedEmptySlider;

	@FXML
	private CheckBox holdEnd;

	@FXML
	private Label holdEndText;

	@FXML
	private Slider holdEndSlider;

	@FXML
	private Button goButton;

	@FXML
	private Button loadFile;

	@FXML
	private Button loadFolder;

	@FXML
	private TextField startDate;

	@FXML
	private TextField endDate;

	@FXML
	private RadioButton formatButtonPlaceholder;

	@FXML
	private TextField outputFolder;

	@FXML
	private TextField outputName;

	private ToggleGroup encoderOptions;
	private ToggleGroup videoFormat;

	@FXML
	private Label fileExtension;

	Stage window;

	private boolean initialized = false;

	private final double[] daysPerFrameOptions = {0.2, 0.25, 0.5, 1, 2, 3, 4, 5, 6, 7, 15, 30};

	private final HashMap<VideoFormats, RadioButton> formatOptions = new HashMap();

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

		videoFormat = new ToggleGroup();
		VBox p = (VBox)formatButtonPlaceholder.getParent();
		p.getChildren().remove(formatButtonPlaceholder);
		for (VideoFormats v : VideoFormats.list) {
			RadioButton b = v.makeNode(formatButtonPlaceholder);
			p.getChildren().add(b);
			b.setToggleGroup(videoFormat);
			b.selectedProperty().addListener(new ChangeListener() {
				@Override
				public void changed(ObservableValue observable, Object oldValue, Object newValue) {
					VideoGuiController.this.update(v.name());
				}
			});
			formatOptions.put(v, b);
			this.registerNode("format"+v.name(), b);
		}
		videoFormat.selectToggle(formatOptions.get(VideoRenderer.instance.videoFormat));
		String at = VideoRenderer.instance.outputPath;
		String folder = at == null ? new File("").getAbsolutePath().replace('\\', '/') : at.substring(0, at.lastIndexOf('/'));
		String file = at == null ? null : at.substring(at.lastIndexOf('/')+1);
		outputFolder.setText(folder);
		outputName.setText(Strings.isNullOrEmpty(file) ? null : file);

		copyToDFX.setSelected(RenderLoop.sendToDFX);

		speedSlider = (Slider)JFXWindow.replaceNode(this, speedSlider, new NonlinearSlider(speedSlider, daysPerFrameOptions, true));

		pauseSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(pauseSlider, newVal.doubleValue()));
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedSlider, newVal.doubleValue()));
		speedEmptySlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(speedEmptySlider, newVal.doubleValue()));
		holdEndSlider.valueProperty().addListener((obs, oldval, newVal) -> this.updateSlider(holdEndSlider, newVal.doubleValue()));

		startDate.setText(Main.getTimeline().getStart().toString());
		endDate.setText(Main.getTimeline().getEnd().toString());

		this.updateSlider(pauseSlider, VideoRenderer.instance.pauseDuration);
		this.updateSlider(speedSlider, ArrayHelper.indexOf(daysPerFrameOptions, VideoRenderer.instance.daysPerFrame));
		this.updateSlider(speedEmptySlider, VideoRenderer.instance.maxSkipSpeed);
		this.updateSlider(holdEndSlider, VideoRenderer.instance.finalHoldTime);

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
				pauseLenText.setText((int)rounded+" seconds");
				break;
			case "speedSlider":
				speedText.setText(MathHelper.fractionalize(daysPerFrameOptions[(int)rounded])+" days per frame");
				break;
			case "speedEmptySlider":
				speedEmptyText.setText((int)rounded+"x");
				break;
			case "holdEndSlider":
				holdEndText.setText(rounded+" seconds");
				break;
		}
		if (initialized)
			this.setSettings();
	}

	@Override
	protected void onButtonClick(Object o, String id) {
		//"E:/My Documents/Programs and Utilities/ffmpeg-4.3.1-full_build/bin/ffmpeg.exe"

		switch(id) {
			case "goButton":
				this.setSettings();
				CalendarRenderer cal = Main.getCalendarRenderer();
				cal.clearSelection();
				JFXWindow.getGUI().setScreenshots(null);
				Main.setVideoInsets();
				VideoRenderer.instance.startRendering(cal);
				window.close();
				return;
			case "loadFile":
				FileChooser fc = new FileChooser();
				File f = fc.showOpenDialog(window);
				boolean valid = f != null && f.exists();
				try {
					mpegPath.setText(valid ? f.getCanonicalPath() : null);
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
				ffmpeg.setSelected(valid);
				break;
			case "loadFolder":
				DirectoryChooser fc2 = new DirectoryChooser();
				File at = new File(outputFolder.getText());
				if (at.exists())
					fc2.setInitialDirectory(at);
				File f2 = fc2.showDialog(window);
				try {
					outputFolder.setText(f2 != null ? f2.getCanonicalPath().replace('\\', '/') : null);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
		this.setSettings();
	}

	private void setSettings() {
		String encoder = this.getNode((Node)encoderOptions.getSelectedToggle()).fxID;
		VideoRenderer.instance.pathToFFMPEG = encoder.equals("ffmpeg") ? mpegPath.getText() : null;
		VideoRenderer.instance.daysPerFrame = daysPerFrameOptions[(int)speedSlider.getValue()];
		VideoRenderer.instance.pauseDuration = pauseNew.isSelected() ? (int)pauseSlider.getValue() : 0;
		VideoRenderer.instance.startDate = DateStamp.parse(startDate.getText());
		VideoRenderer.instance.endDate = DateStamp.parse(endDate.getText());
		VideoRenderer.instance.outputPath = outputFolder.getText()+"/"+outputName.getText();
		VideoRenderer.instance.videoFormat = this.getSelectedFormat();
		VideoRenderer.instance.maxSkipSpeed = speedEmpty.isSelected() ? (int)speedEmptySlider.getValue() : 0;
		VideoRenderer.instance.finalHoldTime = holdEnd.isSelected() ? (int)holdEndSlider.getValue() : 0;
		RenderLoop.sendToDFX = copyToDFX.isSelected();
	}

	private VideoFormats getSelectedFormat() {
		for (Entry<VideoFormats, RadioButton> e : formatOptions.entrySet()) {
			if (e.getValue().isSelected())
				return e.getKey();
		}
		throw new IllegalStateException("No format selected!");
	}

	@Override
	protected void update(String fxID) {
		pauseSlider.setDisable(!pauseNew.isSelected());
		speedEmptySlider.setDisable(!speedEmpty.isSelected());
		holdEndSlider.setDisable(!holdEnd.isSelected());
		fileExtension.setText("."+this.getSelectedFormat().fileExtension);

		if (!pauseNew.isSelected())
			pauseLenText.setText("N/A");
		else
			pauseLenText.setText((int)pauseSlider.getValue()+" seconds");

		if (!holdEnd.isSelected())
			holdEndText.setText("N/A");
		else
			holdEndText.setText(holdEndSlider.getValue()+" seconds");

		if (!speedEmpty.isSelected())
			speedEmptyText.setText("1x");
		else
			speedEmptyText.setText((int)speedEmptySlider.getValue()+"x");

		this.setSettings();
	}

}
