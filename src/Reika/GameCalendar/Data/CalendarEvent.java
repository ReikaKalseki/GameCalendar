package Reika.GameCalendar.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Util.DateStamp;

import javafx.application.HostServices;
import javafx.scene.image.Image;

public abstract class CalendarEvent {

	public final String name;
	public final String description;

	public final ActivityCategory category;
	private final File sourceFile;
	private final HashMap<String, String> data;

	private boolean isMemorable;
	private int privacyLevel = 0;
	private File screenshot;

	private Image screenshotData;

	public CalendarEvent(File f, HashMap<String, String> map, ActivityCategory a) {
		data = new HashMap(map);
		name = data.get("name");
		if (a == null)
			throw new IllegalArgumentException("Null category for '"+name+"'!");
		sourceFile = f;
		data.putAll(a.provideDataOverrides());
		description = data.get("desc");
		category = a;
	}

	public final CalendarEvent setScreenshot(File f) {
		if (!f.exists())
			throw new IllegalArgumentException("Screenshot "+f.getAbsolutePath()+" does not exist!");
		screenshot = f;
		return this;
	}

	public final CalendarEvent setMemorable() {
		isMemorable = true;
		return this;
	}

	public final CalendarEvent setPrivacy(int l) {
		privacyLevel = l;
		return this;
	}

	@Override
	public final String toString() {
		return name+" @ "+this.getDescriptiveDate().toString();
	}

	public abstract DateStamp getDescriptiveDate();

	public abstract int getColor();

	public final Image getScreenshot() {
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

	public final File getScreenshotFile() {
		return screenshot;
	}

	public abstract String getFullDateString();

	public final void openFile(HostServices host) {
		host.showDocument(sourceFile.getAbsolutePath());
	}

	public final boolean isMemorable() {
		return isMemorable;
	}

	public final void generateDescriptionText(ArrayList<String> ret) {
		DateStamp start = this.getFirstDate();
		DateStamp end = this.getLastDate();
		String line = null;
		if (start.equals(end)) {
			line = category.name+": "+name+" ["+start.getFullName(true)+"]";
		}
		else {
			line = category.name+": "+name+" ["+start.getFullName(true)+" to "+end.getFullName(true)+"]";
			if (end.equals(DateStamp.launch))
				line = category.name+": "+name+" [Ongoing since "+start.getFullName(true)+"]";
		}
		if (GuiElement.ARCMERGE.isChecked()) {
			line = line.substring(category.name.length()+2);
		}
		if (this.isMemorable())
			line = line+" (Highly Memorable)";
		ret.add(line);
		if (!Strings.isNullOrEmpty(description)) {
			ret.add("\t"+description);
		}
	}

	protected final boolean isPrivacyLevelVisible() {
		return GuiElement.PRIVACY.getValue() >= privacyLevel;
	}

	protected abstract DateStamp getFirstDate();
	protected abstract DateStamp getLastDate();

	@Override
	public final int hashCode() {
		return name.hashCode() ^ category.hashCode();
	}

	@Override
	public final boolean equals(Object o) {
		return o.getClass() == this.getClass() && ((CalendarEvent)o).name.equals(name) && ((CalendarEvent)o).category.equals(category);
	}

	public int getPrivacy() {
		return privacyLevel;
	}

	public abstract boolean containsYear(int year);

	public final boolean isVisible() {
		return this.isPrivacyLevelVisible() && GuiElement.CATEGORIES.isStringSelected(category.name);
	}

	public final String getProperty(String key) {
		return data.get(key);
	}

}
