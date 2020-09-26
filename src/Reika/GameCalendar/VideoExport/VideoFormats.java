package Reika.GameCalendar.VideoExport;

add radio buttons dynamically for each entry here
public enum VideoFormats {

	X264("mp4", "libx264", "Small size, high quality, not compatible with some players"),
	H264("mp4", ?, "Larger, slower encoding, lower quality, more compatible"),
	AVI("avi", ?, "Huge file size, fastest encoding, lossless, universally compatible"),
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

}
