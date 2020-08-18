package Reika.GameCalendar.GUI;

import java.awt.Polygon;

import Reika.GameCalendar.Data.Section;

class GuiSection {

	public final Section section;
	public Polygon polygon;

	GuiSection(Section s) {
		section = s;
	}

	@Override
	public String toString() {
		return section.toString();
	}

}
