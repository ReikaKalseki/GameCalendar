package Reika.GameCalendar.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import Reika.GameCalendar.Util.DateStamp;

import javafx.scene.image.Image;

public abstract class CalendarEvent {

	public final String name;
	public final String description;

	public final ActivityCategory category;

	private boolean isMemorable;

	private File screenshot;

	private Image screenshotData;

	public CalendarEvent(ActivityCategory a, String n, String desc) {
		if (a == null)
			throw new IllegalArgumentException("Null category for '"+n+"'!");
		category = a;
		name = n;
		description = desc;
	}

	public CalendarEvent setScreenshot(File f) {
		if (!f.exists())
			throw new IllegalArgumentException("Screenshot "+f.getAbsolutePath()+" does not exist!");
		screenshot = f;
		return this;
	}

	public CalendarEvent setMemorable() {
		isMemorable = true;
		return this;
	}

	@Override
	public final String toString() {
		return name+" @ "+this.getDescriptiveDate().toString();
	}

	protected abstract DateStamp getDescriptiveDate();

	public abstract int getColor();

	public Image getScreenshot() {
		if (screenshot != null && screenshotData == null) {
			try(InputStream in = new FileInputStream(screenshot)) {
				screenshotData = new Image(in);
			}
			catch (Exception e) {
				System.err.println("Could not load screenshot '"+screenshot.getAbsolutePath()+"'!");
				e.printStackTrace();
			}
		}
		return screenshotData;
	}

	public File getScreenshotFile() {
		return screenshot;
	}

	public abstract String getFullDateString();

}
