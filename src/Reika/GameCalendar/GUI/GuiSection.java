package Reika.GameCalendar.GUI;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePolygon;

public class GuiSection implements CalendarItem {

	public final Section section;
	public DoublePolygon polygon;

	public final double angleStart;
	public final double angleEnd;

	private GuiSection next;
	public final int index;
	public final GuiSection previous;

	public boolean skipRender;
	public DateStamp renderedEnd;

	private boolean memorable = false;

	private HashSet<ActivityCategory> activeCatCache = null;
	private ArrayList<TimeSpan> activeSpanCache = null;

	public GuiSection(Section s, int idx, GuiSection prev) {
		section = s;
		index = idx;
		previous = prev;
		if (prev != null)
			prev.next = this;

		for (TimeSpan t : s.getSpans()) {
			memorable |= t.isMemorable();
		}

		angleStart = s.startTime.getAngle();
		angleEnd = s.getEnd().getAngle();

		double ae = angleEnd;
		if (ae < angleStart)
			ae += 360;

		if (!s.isEmpty() && !DateStamp.launch.equals(s.getEnd()) && ae-angleStart < 1) {
			System.err.println("Warning: Section @ "+s+" is very small (<1 degrees)!");
		}
	}

	@Override
	public String toString() {
		return section.toString();
	}

	@Override
	public String getDescriptiveDate() {
		return section.getTimeSpan();
	}

	public GuiSection getNext() {
		return next;
	}

	public synchronized Set<ActivityCategory> getActiveCategories() {
		if (activeCatCache == null) {
			activeCatCache = new HashSet();
			for (ActivityCategory ts : section.getCategories()) {
				if (GuiElement.CATEGORIES.isStringSelected(ts.name)) {
					activeCatCache.add(ts);
				}
			}
		}
		return Collections.unmodifiableSet(activeCatCache);
	}

	public synchronized List<TimeSpan> getActiveSpans() {
		if (activeSpanCache == null) {
			activeSpanCache = new ArrayList();
			for (TimeSpan ts : section.getSpans()) {
				if (GuiElement.CATEGORIES.isStringSelected(ts.category.name) && ts.isPrivacyLevelVisible()) {
					activeSpanCache.add(ts);
				}
			}
		}
		return Collections.unmodifiableList(activeSpanCache);
	}

	@Override
	public List<? extends CalendarEvent> getItems(boolean activeOnly) {
		return activeOnly ? this.getActiveSpans() : section.getSpans();
	}

	public boolean isMemorable(boolean activeOnly) {
		return activeOnly ? this.anyActiveMemorable() : memorable;
	}

	private boolean anyActiveMemorable() {
		for (TimeSpan t : this.getActiveSpans()) {
			if (t.isMemorable())
				return true;
		}
		return false;
	}

	public void clearCache() {
		activeCatCache = null;
		activeSpanCache = null;
	}

	/** INCLUSIVE */
	public boolean containsDate(DateStamp date, boolean anyYear) {
		DateStamp end = renderedEnd != null ? renderedEnd : section.getEnd();
		if (anyYear) {
			if (this.getLength() > 366)
				return true;
			MonthDay current = MonthDay.of(date.month, date.day);
			MonthDay from = MonthDay.of(section.startTime.month, section.startTime.day);
			MonthDay until = MonthDay.of(end.month, end.day);

			if (from.compareTo(until) <= 0)
				return from.compareTo(current) <= 0 && current.compareTo(until) <= 0;
			else
				return current.compareTo(until) <= 0 || current.compareTo(from) >= 0;
		}
		else {
			return date.isBetween(section.startTime, end);
		}
	}

	public int getLength() {
		DateStamp end = renderedEnd != null ? renderedEnd : section.getEnd();
		return section.startTime.countDaysAfter(end);
	}

	@Override
	public boolean containsYear(int year, boolean activeOnly) {
		for (TimeSpan ts : activeOnly ? this.getActiveSpans() : section.getSpans()) {
			if (ts.start.year == year || ts.end.year == year)
				return true;
		}
		return false;
	}

}
