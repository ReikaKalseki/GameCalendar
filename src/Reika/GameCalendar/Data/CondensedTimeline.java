package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Reika.GameCalendar.GUI.CalendarItem;
import Reika.GameCalendar.GUI.CalendarSection;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Util.DateStamp;

public class CondensedTimeline {

	private final HashMap<ActivityCategory, CondensedSection> lastSections = new HashMap();
	private final ArrayList<CondensedSection> sections = new ArrayList();

	public CondensedTimeline(Timeline t, int maxGap) {
		if (!t.isPrepared()) {
			throw new IllegalArgumentException("A timeline can only be condensed once it is prepared!");
		}
		for (TimeSpan sp : t.getPeriods()) {
			this.addOrMergeSection(sp, maxGap);
		}
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

	public void refresh() {
		for (CondensedSection s : sections) {
			s.polygon = null;
		}
	}

	private static class CondensedSection extends CalendarSection implements CalendarItem {

		private final ActivityCategory category;

		private final ArrayList<TimeSpan> spans = new ArrayList();

		private DateStamp end;
		private double angleEnd;

		public CondensedSection(ActivityCategory a, DateStamp s) {
			super(s);
			category = a;
		}

		private void extendTo(TimeSpan s) {
			spans.add(s);
			if (end.compareTo(s.end) < 0) {
				end = s.end;
				angleEnd = end.getAngle();
			}
		}

		@Override
		public String getDescriptiveDate() {
			return start.toString();
		}

		@Override
		public HashSet<ActivityCategory> getActiveCategories() {
			HashSet<ActivityCategory> set = new HashSet();
			if (GuiElement.CATEGORIES.isStringSelected(category.name)) {
				set.add(category);
			}
			return set;
		}

		@Override
		public List<? extends CalendarEvent> getItems(boolean activeOnly) {
			return !activeOnly || GuiElement.CATEGORIES.isStringSelected(category.name) ? Collections.unmodifiableList(spans) : new ArrayList();
		}

		@Override
		public DateStamp getEnd() {
			return end;
		}

		@Override
		public double getEndAngle() {
			return angleEnd;
		}

	}

}
