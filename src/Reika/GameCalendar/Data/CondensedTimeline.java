package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Reika.GameCalendar.Util.DateStamp;

public class CondensedTimeline {

	private final HashMap<ActivityCategory, CondensedSection> lastSections = new HashMap();
	private final ArrayList<CondensedSection> sections = new ArrayList();

	private final Timeline assembled;

	public CondensedTimeline(Timeline t, int maxGap) {
		if (!t.isPrepared()) {
			throw new IllegalArgumentException("A timeline can only be condensed once it is prepared!");
		}
		for (TimeSpan sp : t.getPeriods()) {
			this.addOrMergeSection(sp, maxGap);
		}
		Collections.sort(sections);
		assembled = new Timeline();
		for (CondensedSection s : sections) {
			assembled.addPeriod(s.getAsSpan());
		}
		assembled.prepare();
	}

	private void addOrMergeSection(TimeSpan s, int maxGap) {
		CondensedSection last = lastSections.get(s.category);
		if (last != null && last.end.countDaysAfter(s.start) > maxGap) {
			lastSections.remove(s.category);
			last = null;
		}
		if (last == null) {
			last = new CondensedSection(s.category, s.start);
			sections.add(last);
			lastSections.put(s.category, last);
		}
		last.extendTo(s);
	}

	public List<Section> getSections() {
		return assembled.getSections();
	}

	private static class CondensedSection implements Comparable<CondensedSection> {

		private final ActivityCategory category;
		private final DateStamp start;

		private final ArrayList<TimeSpan> spans = new ArrayList();

		private DateStamp end;

		public CondensedSection(ActivityCategory a, DateStamp s) {
			start = s;
			category = a;
		}

		private TimeSpan getAsSpan() {
			return new TimeSpan(null, category, start, end, category.name, category.desc);
		}

		private void extendTo(TimeSpan s) {
			spans.add(s);
			if (end == null || end.compareTo(s.end) < 0) {
				end = s.end;
			}
		}

		@Override
		public int compareTo(CondensedSection o) {
			return start.compareTo(o.start);
		}

	}

}
