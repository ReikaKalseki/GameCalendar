package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jcodec.api.awt.AWTSequenceEncoder;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.GuiHighlight;
import Reika.GameCalendar.GUI.GuiSection;
import Reika.GameCalendar.GUI.StatusHandler;

public class VideoRenderer {

	public static final VideoRenderer instance = new VideoRenderer();

	private static final int CALENDAR_SIZE = 800;
	private static final int SCREENSHOT_WIDTH = 480;
	private static final int SCREENSHOT_HEIGHT = 270;
	private static final int VIDEO_WIDTH = 1760;
	private static final int VIDEO_HEIGHT = 1080;

	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;
	private AWTSequenceEncoder encoder;
	private Framebuffer screenshotsMSAA;
	private Framebuffer screenshotHolder;

	private VideoRenderer() {

	}

	public void startRendering(CalendarRenderer data) {
		try {
			if (screenshotsMSAA == null)
				screenshotsMSAA = new Framebuffer(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, true);
			if (screenshotHolder == null)
				screenshotHolder = new Framebuffer(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT);

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

	public void addFrame(Framebuffer calendar) {
		BufferedImage frame = new BufferedImage(VIDEO_WIDTH, VIDEO_HEIGHT, BufferedImage.TYPE_INT_RGB);
		calendar.writeIntoImage(frame, 0, 0);
		ArrayList<File> li = this.getCurrentScreenshots();
		for (int i = 0; i < li.size(); i++) {
			File img = li.get(i);
			screenshotsMSAA.loadImage(img);
			screenshotsMSAA.sendTo(screenshotHolder);
			int ox = SCREENSHOT_WIDTH*(i%2);
			int oy = SCREENSHOT_HEIGHT*(i/2);
			int x = CALENDAR_SIZE+ox;
			int y = oy;
			screenshotHolder.writeIntoImage(frame, x, y);
		}
		try {
			encoder.encodeImage(frame);
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

	private ArrayList<File> getCurrentScreenshots() {
		ArrayList<File> ret = new ArrayList();
		GuiSection s = renderer.getSectionAt(renderer.limit);
		if (s != null) {
			ArrayList<CalendarEvent> li = new ArrayList();
			li.addAll(s.getItems(false));
			Collection<GuiHighlight> c = renderer.getHighlightsInSection(s);
			for (GuiHighlight h : c) {
				li.addAll(h.getItems(false));
			}
			Collections.sort(li, CalendarRenderer.eventSorter);
			for (CalendarEvent e : li) {
				File img = e.getScreenshotFile();
				ret.add(img);
			}
		}
		return ret;
	}

}
