package Reika.GameCalendar.GUI;

import java.util.List;

import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePolygon;

public class GuiSection implements CalendarItem {

	public final Section section;
	public DoublePolygon polygon;

	public final double angleStart;
	public final double angleEnd;

	public GuiSection(Section s) {
		section = s;

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
	public List<String> generateDescription() {
		return section.generateDescription();
	}

	@Override
	public String getDescriptiveDate() {
		return section.getTimeSpan();
	}

}
