package Reika.GameCalendar.Data;

import java.io.File;
import java.util.ArrayList;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Util.DateStamp;

public class Highlight extends CalendarEvent implements Comparable<Highlight> {

	public final DateStamp time;

	public Highlight(File f, ActivityCategory a, DateStamp t, String n, String desc) {
		super(f, a, n, desc);
		time = t;
	}

	@Override
	public int compareTo(Highlight o) {
		return time.compareTo(o.time);
	}

	@Override
	public DateStamp getDescriptiveDate() {
		return time;
	}

	@Override
	public int getColor() {
		return 0x000000;
	}

	@Override
	public String getFullDateString() {
		return this.getDescriptiveDate().toString();
	}

	@Override
	public void generateDescriptionText(ArrayList<String> ret) {
		String line = category.name+": "+name+" ["+time+"]";
		if (this.isMemorable())
			line = line+" (Highly Memorable)";
		ret.add(line);
		if (!Strings.isNullOrEmpty(description)) {
			ret.add("\t"+description);
		}
	}

}
