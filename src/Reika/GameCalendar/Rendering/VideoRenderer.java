package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.jcodec.api.awt.AWTSequenceEncoder;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.StatusHandler;

public class VideoRenderer {

	public static final VideoRenderer instance = new VideoRenderer();

	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;
	private AWTSequenceEncoder encoder;

	private VideoRenderer() {

	}

	public void startRendering(CalendarRenderer data) {
		try {
			encoder = AWTSequenceEncoder.createSequenceEncoder(new File("E:/videotest22.mp4"), 60);

			isRendering = true;
			renderer = data;
			time = Main.getTimeline();
			renderer.limit = time.getStart();

			StatusHandler.postStatus("Rendering video...", 999999999);
		}
		catch (IOException e) {
			e.printStackTrace();

			StatusHandler.postStatus("Video creation failed.", 2500, false);
		}
	}

	public boolean isRendering() {
		return isRendering;
	}

	public void addFrame(Framebuffer fb) {
		try {
			BufferedImage img = fb.toImage(1024, 1024);
			encoder.encodeImage(img);
		}
		catch (IOException e) {
			e.printStackTrace();
			renderer.limit = null;
			isRendering = false;
			StatusHandler.postStatus("Video frame construction failed.", 2500, false);
		}

		if (renderer.limit.equals(time.getEnd())) {
			this.finish();
			renderer.limit = null;
		}
		else {
			renderer.limit = renderer.limit.nextDay();
		}
	}

	private void finish() {
		try {
			encoder.finish();
			StatusHandler.postStatus("Video completion succeeded.", 2500, false);
		}
		catch(Exception e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video completion failed.", 2500, false);
		}

		isRendering = false;
	}

}
