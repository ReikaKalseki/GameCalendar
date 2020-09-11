package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Data.ActivityCategory.SortingMode;
import Reika.GameCalendar.Util.DateStamp;

import javafx.application.Platform;

public class Timeline {

	private final ArrayList<Highlight> events = new ArrayList();
	private final ArrayList<TimeSpan> periods = new ArrayList();

	private final HashSet<DateStamp> dates = new HashSet();
	private final HashSet<Integer> years = new HashSet();

	private final ArrayList<Section> sections = new ArrayList();

	private DateStamp earliest;
	private DateStamp latest;

	public void addEvent(Highlight e) {
		events.add(e);
		dates.add(e.time);
		years.add(e.time.year);
		this.updateBounds(e.time);
	}

	public void addPeriod(TimeSpan e) {
		periods.add(e);
		dates.add(e.start);
		dates.add(e.end);
		years.add(e.start.year);
		years.add(e.end.year);
		this.updateBounds(e.start, e.end);
	}

	private void updateBounds(DateStamp t) {
		this.updateBounds(t, t);
	}

	private void updateBounds(DateStamp t1, DateStamp t2) {
		if (earliest == null || t1.compareTo(earliest) < 0)
			earliest = t1;
		if (latest == null || t2.compareTo(latest) > 0)
			latest = t2;
	}

	public void prepare() {
		Collections.sort(events);
		Collections.sort(periods);
		ArrayList<DateStamp> li = new ArrayList(dates);
		Collections.sort(li);
		HashSet<TimeSpan> activeSpans = new HashSet();
		for (DateStamp s : li) {
			for (TimeSpan t : periods) {
				if (t.start.equals(s)) {
					activeSpans.add(t);
					this.splitSection(s, activeSpans);
				}
				else if (t.end.equals(s)) {
					activeSpans.remove(t);
					this.splitSection(s, activeSpans);
				}
			}
		}
		sections.get(sections.size()-1).setEndTime(latest);
		Collections.sort(sections);

		if (false) {
			ArrayList<CalendarEvent> check = new ArrayList();
			check.addAll(periods);
			check.addAll(events);
			boolean flag = false;
			Collections.sort(check, new Comparator<CalendarEvent>(){
				@Override
				public int compare(CalendarEvent o1, CalendarEvent o2) {
					return o1.category.compareTo(SortingMode.ALPHA, o2.category);
				}
			});
			for (CalendarEvent t : check) {
				if (t.getScreenshotFile() == null) {
					System.out.println("Calendar Item "+t.category.name+"\\"+t.name+" ["+t.getFullDateString()+"] has no screenshot!");
					flag = true;
				}
			}
			if (flag) {
				Platform.exit();
				System.exit(0);
			}
		}
	}

	private void splitSection(DateStamp at, HashSet<TimeSpan> active) {
		if (!sections.isEmpty())
			sections.get(sections.size()-1).setEndTime(at);
		//if (!at.equals(earliest))
		sections.add(new Section(at, active));
	}

	public Section getSection(DateStamp at) {
		if (at.compareTo(earliest) < 0)
			throw new IllegalArgumentException("Date "+at+" is before the timeline begins!");
		if (at.compareTo(latest) > 0)
			throw new IllegalArgumentException("Date "+at+" is after the timeline ends!");
		for (int i = 0; i < sections.size()-1; i++) {
			Section s1 = sections.get(i);
			Section s2 = sections.get(i+1);
			if (s1.startTime.compareTo(at) <= 0 && s2.startTime.compareTo(at) >= 0)
				return s1;
		}
		throw new IllegalStateException("Never found a matching section?!");
	}

	public List<Section> getSections() {
		return Collections.unmodifiableList(sections);
	}

	public List<Highlight> getEvents() {
		return Collections.unmodifiableList(events);
	}

	public Set<Integer> getYears() {
		return Collections.unmodifiableSet(years);
	}

	public DateStamp getStart() {
		return earliest;
	}

	public DateStamp getEnd() {
		return latest;
	}

}
