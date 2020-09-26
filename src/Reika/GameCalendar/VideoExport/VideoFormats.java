package Reika.GameCalendar.VideoExport;

import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

public enum VideoFormats {

	X264("mp4", "libx264", "Small size, high quality, not compatible with some players"),
	H264("mp4", "libopenh264", "Larger, slower encoding, lower quality, more compatible"),
	AVI("avi", "avi -b 700k -qscale 0 -ab 160k -ar 44100", "Huge file size, fastest encoding, lossless, universally compatible"),
	;

	public final String ffmpegArgs;
	public final String fileExtension;
	private final String desc;

	public static final VideoFormats[] list = values();

	private VideoFormats(String e, String a, String d) {
		ffmpegArgs = a;
		fileExtension = e;
		desc = d;
	}

	public String getButtonText() {
		return this.name()+" ("+desc+")";
	}

	public RadioButton makeNode(RadioButton template) {
		RadioButton ret = new RadioButton();
		ret.setText(this.getButtonText());
		ret.setPadding(template.getPadding());
		VBox.setMargin(ret, VBox.getMargin(template));
		ret.setFont(template.getFont());
		ret.setAlignment(template.getAlignment());
		ret.setNodeOrientation(template.getNodeOrientation());
		ret.setMinSize(template.getMinWidth(), template.getMinHeight());
		ret.setMaxSize(template.getMaxWidth(), template.getMaxHeight());
		ret.setPrefSize(template.getPrefWidth(), template.getPrefHeight());
		ret.setTextAlignment(template.getTextAlignment());
		return ret;
	}

}
