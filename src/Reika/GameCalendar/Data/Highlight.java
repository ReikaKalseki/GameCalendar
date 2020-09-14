package Reika.GameCalendar.Data;

import java.io.File;

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
	protected DateStamp getFirstDate() {
		return time;
	}

	@Override
	protected DateStamp getLastDate() {
		return time;
	}

}
