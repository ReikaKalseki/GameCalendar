package Reika.GameCalendar.Data;

import java.io.File;

import Reika.GameCalendar.Rendering.Texture;
import Reika.GameCalendar.Util.DateStamp;

public abstract class CalendarEvent {

	public final String name;
	public final String description;

	public final ActivityCategory category;

	private File screenshot;

	private Texture screenshotData;

	public CalendarEvent(ActivityCategory a, String n, String desc) {
		if (a == null)
			throw new IllegalArgumentException("Null category for '"+n+"'!");
		category = a;
		name = n;
		description = desc;
	}

	public CalendarEvent setScreenshot(File f) {
		screenshot = f;
		return this;
	}

	@Override
	public final String toString() {
		return name+" @ "+this.getDescriptiveDate().toString();
	}

	protected abstract DateStamp getDescriptiveDate();

	public abstract int getColor();

	public void bindScreenshot() {
		if (screenshot != null) {
			if (screenshotData == null) {
				screenshotData = new Texture(screenshot, true);
			}
			screenshotData.bind();
		}
	}

}
