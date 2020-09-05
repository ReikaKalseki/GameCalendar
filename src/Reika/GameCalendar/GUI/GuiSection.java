package Reika.GameCalendar.GUI;

import java.util.List;

import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Util.DoublePolygon;

public class GuiSection implements CalendarItem {

	public final Section section;
	public DoublePolygon polygon;

	public GuiSection(Section s) {
		section = s;
	}

	@Override
	public String toString() {
		return section.toString();
	}

	@Override
	public List<String> generateDescription() {
		return section.generateDescription();
	}

}
