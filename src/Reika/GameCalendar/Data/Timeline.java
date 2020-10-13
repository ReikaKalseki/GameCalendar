package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import Reika.GameCalendar.Data.ActivityCategory.SortingMode;
import Reika.GameCalendar.Util.DateStamp;

import javafx.application.Platform;

public class Timeline {

	private final ArrayList<Highlight> events = new ArrayList();
	private final ArrayList<TimeSpan> periods = new ArrayList();

	private final HashSet<DateStamp> dates = new HashSet();
	private final HashSet<Integer> years = new HashSet();

	private final ArrayList<Section> sections = new ArrayList();
	private final ArrayList<Section> sectionsByCategory = new ArrayList();

	private DateStamp earliest;
	private DateStamp latest;
	private int maxPrivacy = 0;

	private final TreeMap<DateStamp, Integer> memorabilityGraph = new TreeMap();
	private final HashMap<ActivityCategory, ActivityValue> activityLevels = new HashMap();

	private boolean prepared = false;

	private static int screenshotFilter = 0;

	public void addEvent(Highlight e) {
		events.add(e);
		dates.add(e.time);
		years.add(e.time.year);
		this.updateBounds(e.time, e.getPrivacy());
		prepared = false;
	}

	public void addPeriod(TimeSpan e) {
		periods.add(e);
		dates.add(e.start);
		dates.add(e.end);
		years.add(e.start.year);
		years.add(e.end.year);
		this.updateBounds(e.start, e.end, e.getPrivacy());
		prepared = false;
	}

	private void updateBounds(DateStamp t, int p) {
		this.updateBounds(t, t, p);
	}

	private void updateBounds(DateStamp t1, DateStamp t2, int p) {
		if (earliest == null || t1.compareTo(earliest) < 0)
			earliest = t1;
		if (latest == null || t2.compareTo(latest) > 0)
			latest = t2;
		maxPrivacy = Math.max(maxPrivacy, p);
	}

	public void prepare() {
		Collections.sort(events);
		Collections.sort(periods);
		ArrayList<DateStamp> li = new ArrayList(dates);
		Collections.sort(li);
		HashSet<TimeSpan> activeSpans = new HashSet();
		ArrayList<ActivityPoint> snapshots = new ArrayList();
		for (DateStamp s : li) {
			for (TimeSpan t : periods) {
				if (t.start.equals(s)) {
					activeSpans.add(t);
					this.splitSection(s, activeSpans, snapshots);
				}
				else if (t.end.equals(s)) {
					activeSpans.remove(t);
					this.splitSection(s, activeSpans, snapshots);
				}
			}
		}
		sections.get(sections.size()-1).setEndTime(latest);
		Collections.sort(sections);

		for (ActivityCategory a : ActivityCategory.getAllCategories()) {
			int last = -1;
			ActivityValue av = new ActivityValue();
			for (ActivityPoint p : snapshots) {
				int has = p.getValue(a);
				if (has != last) {
					av.addPoint(p.date, has);
				}
				last = has;
			}
			activityLevels.put(a, av);
		}

		prepared = true;

		if (screenshotFilter > 0) {
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
				if (t instanceof CompoundElement)
					continue;
				if (t.getScreenshotFile() == null) {
					System.out.println("Calendar Item "+t.category.name+"\\"+t.name+" ["+t.getFullDateString()+"] has no screenshot!");
					flag = true;
				}
			}
			if (flag && screenshotFilter == 2) {
				Platform.exit();
				System.exit(0);
			}
		}
	}

	private void splitSection(DateStamp at, HashSet<TimeSpan> active, ArrayList<ActivityPoint> snapshots) {
		if (!sections.isEmpty())
			sections.get(sections.size()-1).setEndTime(at);
		//if (!at.equals(earliest))
		sections.add(new Section(at, active));
		int mem = 0;
		HashMap<ActivityCategory, Integer> map = new HashMap();
		for (TimeSpan ts : active) {
			if (ts.isMemorable())
				mem++;
			Integer get = map.get(ts.category);
			int has = get != null ? get.intValue() : 0;
			map.put(ts.category, has+1);
		}
		memorabilityGraph.put(at, mem);
		snapshots.add(new ActivityPoint(at, map));
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

	public Set<Integer> getYears() {
		return Collections.unmodifiableSet(years);
	}

	public DateStamp getStart() {
		return earliest;
	}

	public DateStamp getEnd() {
		return latest;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public List<TimeSpan> getPeriods() {
		return Collections.unmodifiableList(periods);
	}

	public List<Highlight> getEvents() {
		return Collections.unmodifiableList(events);
	}

	public int getMaxPrivacyLevel() {
		return maxPrivacy;
	}

	public Map<DateStamp, Integer> getMemorabilityGraph() {
		return Collections.unmodifiableMap(memorabilityGraph);
	}

	private static class ActivityPoint {

		private final DateStamp date;
		private final HashMap<ActivityCategory, Integer> data;

		private ActivityPoint(DateStamp at, HashMap<ActivityCategory, Integer> map) {
			date = at;
			data = map;
		}

		public int getValue(ActivityCategory a) {
			Integer get = data.get(a);
			return get != null ? get.intValue() : 0;
		}

	}

}
