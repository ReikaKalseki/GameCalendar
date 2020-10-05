package Reika.GameCalendar.GUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePoint;

public class GuiHighlight implements CalendarItem {

	private final ArrayList<Highlight> events = new ArrayList();
	public final DateStamp time;

	private boolean memorable = false;

	public DoublePoint position;

	private HashSet<ActivityCategory> activeCatCache = null;
	private ArrayList<Highlight> activeEventCache = null;

	public GuiHighlight(Highlight... h) {
		if (h.length == 0)
			throw new IllegalArgumentException("You cannot create an empty highlight!");
		time = h[0].time;
		for (int i = 0; i < h.length; i++) {
			this.addEvent(h[i]);
		}
	}

	public void addEvent(Highlight s) {
		if (!time.equals(s.time))
			throw new IllegalArgumentException("You cannot group highlights of different days!");
		events.add(s);
		memorable |= s.isMemorable();
	}

	@Override
	public String getDescriptiveDate() {
		return events.get(0).time.toString();
	}

	public synchronized Set<ActivityCategory> getActiveCategories() {
		if (activeCatCache == null) {
			activeCatCache = new HashSet();
			for (Highlight ts : events) {
				if (GuiElement.CATEGORIES.isStringSelected(ts.category.name)) {
					activeCatCache.add(ts.category);
				}
			}
		}
		return Collections.unmodifiableSet(activeCatCache);
	}

	@Override
	public List<? extends CalendarEvent> getItems(boolean activeOnly) {
		return activeOnly ? this.getActiveEvents() : Collections.unmodifiableList(events);
	}

	public synchronized List<Highlight> getActiveEvents() {
		if (activeEventCache == null) {
			activeEventCache = new ArrayList();
			for (Highlight ts : events) {
				if (ts.isVisible()) {
					activeEventCache.add(ts);
				}
			}
		}
		return Collections.unmodifiableList(activeEventCache);
	}

	public boolean isMemorable(boolean activeOnly) {
		return activeOnly ? this.anyActiveMemorable() : memorable;
	}

	private boolean anyActiveMemorable() {
		for (Highlight t : this.getActiveEvents()) {
			if (t.isMemorable())
				return true;
		}
		return false;
	}

	public void clearCache() {
		activeCatCache = null;
		activeEventCache = null;
	}

	@Override
	public boolean containsYear(int year, boolean activeOnly) {
		return time.year == year && (!activeOnly || !this.getActiveEvents().isEmpty());
	}

}
