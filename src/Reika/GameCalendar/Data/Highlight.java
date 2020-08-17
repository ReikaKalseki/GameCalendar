package Reika.GameCalendar.Data;

import Reika.GameCalendar.Util.DateStamp;

public class Highlight extends CalendarEvent implements Comparable<Highlight> {

	public final DateStamp time;

	public Highlight(DateStamp t, String n, String desc) {
		super(n, desc);
		time = t;
	}

	@Override
	public int compareTo(Highlight o) {
		return time.compareTo(o.time);
	}

	@Override
	protected DateStamp getDescriptiveDate() {
		return time;
	}

}
