package Reika.GameCalendar.Data;

import java.io.File;
import java.util.HashMap;

import Reika.GameCalendar.Util.DateStamp;

public class Highlight extends CalendarEvent implements Comparable<Highlight> {

	public final DateStamp time;

	public Highlight(File f, HashMap<String, String> data, ActivityCategory a, DateStamp t) {
		super(f, data, a);
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
	protected DateStamp getFirstDate() {
		return time;
	}

	@Override
	protected DateStamp getLastDate() {
		return time;
	}

	@Override
	public boolean containsYear(int year) {
		return time.year == year;
	}

}
