package Reika.GameCalendar.GUI;

import java.util.Collection;
import java.util.HashSet;

import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.Util.DateStamp;

public class Section implements Comparable<Section> {

	public final DateStamp startTime;
	private DateStamp endTime;
	private final HashSet<TimeSpan> activeSpans;
	private final HashSet<String> nameSet = new HashSet();

	public Section(DateStamp start, Collection<TimeSpan> active) {
		startTime = start;
		activeSpans = new HashSet(active);
		for (TimeSpan t : active) {
			nameSet.add(t.name);
		}
	}

	public void setEndTime(DateStamp s) {
		endTime = s;
		if (endTime.compareTo(startTime) < 0)
			throw new IllegalArgumentException("Section "+this+" would have negative length if ended at "+s);
	}

	@Override
	public int compareTo(Section o) {
		return startTime.compareTo(o.startTime);
	}

	@Override
	public String toString() {
		return startTime+": "+nameSet;
	}

	public DateStamp getEnd() {
		return endTime;
	}

}
