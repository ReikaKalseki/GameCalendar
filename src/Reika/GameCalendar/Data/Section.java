package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.Util.DateStamp;

public class Section implements Comparable<Section> {

	public final DateStamp startTime;
	private DateStamp endTime;
	private final ArrayList<TimeSpan> activeSpans;
	private final HashSet<String> nameSet = new HashSet();
	private final HashSet<ActivityCategory> catSet = new HashSet();

	public Section(DateStamp start, Collection<TimeSpan> active) {
		startTime = start;
		activeSpans = new ArrayList(active);
		Collections.sort(activeSpans);
		for (TimeSpan t : active) {
			nameSet.add(t.name);
			if (!catSet.add(t.category)) {
				System.out.println("Note: Section @ "+startTime+" has multiple overlapping regions for category '"+t.category.name+"'!");
			}
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
		return startTime+"-"+endTime+": "+nameSet;
	}

	public DateStamp getEnd() {
		return endTime;
	}

	public boolean isEmpty() {
		return activeSpans.isEmpty();
	}

	public List<TimeSpan> getActiveSpans() {
		return Collections.unmodifiableList(activeSpans);
	}

	public Set<ActivityCategory> getCategories() {
		return Collections.unmodifiableSet(catSet);
	}

	public ArrayList<String> generateDescription() {
		ArrayList<TimeSpan> li = new ArrayList();
		for (TimeSpan ts : activeSpans) {
			if (JFXWindow.getGUI().isListEntrySelected("catList", ts.category.name)) {
				li.add(ts);
			}
		}
		ArrayList<String> ret = new ArrayList();
		Collections.sort(li, new Comparator<TimeSpan>() {

			@Override
			public int compare(TimeSpan o1, TimeSpan o2) {
				return o1.category.compareTo(JFXWindow.getGUI().getSortingMode(), o2.category);
			}

		});
		for (int i = 0; i < li.size(); i++) {
			TimeSpan ts = li.get(i);
			String line = ts.category.name+": "+ts.name+" ["+ts.start+" - "+ts.end+"]";
			ret.add(line);
			if (!Strings.isNullOrEmpty(ts.description)) {
				ret.add("\t"+ts.description);
			}
			if (i < activeSpans.size()-1) {
				ret.add("");
			}
		}
		return ret;
	}

	public String getTimeSpan() {
		return startTime.toString()+" - "+endTime.toString();
	}

}
