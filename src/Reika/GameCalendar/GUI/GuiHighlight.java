package Reika.GameCalendar.GUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePoint;

public class GuiHighlight implements CalendarItem {

	private final ArrayList<Highlight> events = new ArrayList();
	public final DateStamp time;

	public DoublePoint position;

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
	}

	@Override
	public String getDescriptiveDate() {
		return events.get(0).time.toString();
	}

	public ArrayList<String> generateDescription() {
		ArrayList<Highlight> li = this.getActiveEvents();
		ArrayList<String> ret = new ArrayList();
		Collections.sort(li, new Comparator<Highlight>() {

			@Override
			public int compare(Highlight o1, Highlight o2) {
				return o1.category.compareTo(JFXWindow.getGUI().getSortingMode(), o2.category);
			}

		});
		for (int i = 0; i < li.size(); i++) {
			Highlight ts = li.get(i);
			String line = ts.category.name+": "+ts.name+" ["+ts.time+"]";
			ret.add(line);
			if (!Strings.isNullOrEmpty(ts.description)) {
				ret.add("\t"+ts.description);
			}
			if (i < events.size()-1) {
				ret.add("");
			}
		}
		return ret;
	}

	//TODO cache this
	public HashSet<ActivityCategory> getActiveCategories() {
		HashSet<ActivityCategory> set = new HashSet();
		for (Highlight ts : events) {
			if (GuiElement.CATEGORIES.isStringSelected(ts.category.name)) {
				set.add(ts.category);
			}
		}
		return set;
	}

	@Override
	public List<? extends CalendarEvent> getItems(boolean activeOnly) {
		return activeOnly ? this.getActiveEvents() : Collections.unmodifiableList(events);
	}

	//TODO cache this
	public ArrayList<Highlight> getActiveEvents() {
		ArrayList<Highlight> li = new ArrayList();
		for (Highlight ts : events) {
			if (GuiElement.CATEGORIES.isStringSelected(ts.category.name)) {
				li.add(ts);
			}
		}
		return li;
	}

}
