package Reika.GameCalendar.Data;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Util.DateStamp;

public class TimeSpan extends CalendarEvent implements Comparable<TimeSpan> {

	public final DateStamp start;
	public final DateStamp end;

	public boolean isContinuous = true;

	public TimeSpan(File f, ActivityCategory a, DateStamp s, DateStamp e, String n, String desc) {
		super(f, a, n, desc);
		start = s;
		end = e;
		if (start.compareTo(end) >= 0) {
			throw new IllegalArgumentException("Span ("+s+" > "+e+") is zero or negative!");
		}
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
	public void generateDescriptionText(ArrayList<String> ret) {
		String line = category.name+": "+name+" ["+start+" - "+end+"]";
		if (end.equals(DateStamp.launch))
			line = category.name+": "+name+" ["+start+", ongoing]";
		ret.add(line);
		if (!Strings.isNullOrEmpty(description)) {
			ret.add("\t"+description);
		}
	}

}
