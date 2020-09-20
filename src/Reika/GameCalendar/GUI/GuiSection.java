package Reika.GameCalendar.GUI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

	//TODO cache this
	public HashSet<ActivityCategory> getActiveCategories() {
		HashSet<ActivityCategory> set = new HashSet();
		for (ActivityCategory ts : section.getCategories()) {
			if (GuiElement.CATEGORIES.isStringSelected(ts.name)) {
				set.add(ts);
			}
		}
		return set;
	}

	//TODO - cache this
	public ArrayList<TimeSpan> getActiveSpans() {
		ArrayList<TimeSpan> li = new ArrayList();
		for (TimeSpan ts : section.getSpans()) {
			if (GuiElement.CATEGORIES.isStringSelected(ts.category.name)) {
				li.add(ts);
			}
		}
		return li;
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

}
