package Reika.GameCalendar.Rendering;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.Timeline;

public class VideoRenderer {

	public static final VideoRenderer instance = new VideoRenderer();

	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;

	private VideoRenderer() {

	}

	public void startRendering(CalendarRenderer data) {
		isRendering = true;
		renderer = data;
		time = Main.getTimeline();
		renderer.limit = time.getStart();
	}

	public boolean isRendering() {
		return isRendering;
	}

	public void addFrame(Framebuffer fb) {
		if (renderer.limit.equals(time.getEnd())) {
			this.finish();
		}
		else {
			renderer.limit = renderer.limit.nextDay();
		}
	}

	private void finish() {
		isRendering = false;
	}

}
