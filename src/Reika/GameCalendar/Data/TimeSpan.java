package Reika.GameCalendar.Data;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import Reika.GameCalendar.Util.DateStamp;

public class TimeSpan extends CalendarEvent implements Comparable<TimeSpan> {

	public final DateStamp start;
	public final DateStamp end;

	public TimeSpan(File f, HashMap<String, String> data, ActivityCategory a, DateStamp s, DateStamp e) {
		super(f, data, a);
		start = s;
		end = e;
		if (s == null)
			throw new IllegalArgumentException("No start time specified!");
		if (e == null)
			throw new IllegalArgumentException("No end time specified!");
		if (start.compareTo(end) >= 0 && !this.isOngoing()) {
			throw new IllegalArgumentException("Span ("+s+" > "+e+") is zero or negative!");
		}
	}

	public boolean isOngoing() {
		return end.equals(DateStamp.launch);
	}

	public int lengthInDays() {
		LocalDate ld1 = start.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate ld2 = end.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return (int)ChronoUnit.DAYS.between(ld1, ld2);
	}

	@Override
	public int compareTo(TimeSpan o) {
		return start.compareTo(o.start);
	}

	@Override
	public DateStamp getDescriptiveDate() {
		return start;
	}

	@Override
	public int getColor() {
		return category.color;
	}

	@Override
	public String getFullDateString() {
		return start.toString()+" - "+end.toString();
	}

	@Override
	protected DateStamp getFirstDate() {
		return start;
	}

	@Override
	protected DateStamp getLastDate() {
		return end;
	}

	@Override
	public boolean containsYear(int year) {
		return start.year == year || end.year == year || (start.year < year && end.year > year);
	}

}
