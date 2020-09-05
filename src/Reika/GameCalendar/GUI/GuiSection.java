package Reika.GameCalendar.GUI;

import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Util.DoublePolygon;

public class GuiSection {

	public final Section section;
	public DoublePolygon polygon;

	public GuiSection(Section s) {
		section = s;
	}

	@Override
	public String toString() {
		return section.toString();
	}

}
