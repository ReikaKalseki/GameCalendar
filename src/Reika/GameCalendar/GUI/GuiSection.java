package Reika.GameCalendar.GUI;

import java.awt.Polygon;

import Reika.GameCalendar.Data.Section;

public class GuiSection {

	public final Section section;
	public Polygon polygon;

	public GuiSection(Section s) {
		section = s;
	}

	@Override
	public String toString() {
		return section.toString();
	}

}
