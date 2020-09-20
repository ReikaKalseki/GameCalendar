package Reika.GameCalendar.GUI;

import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePolygon;

public abstract class CalendarSection implements CalendarItem {

	public final DateStamp start;
	public final double angleStart;

	public DoublePolygon polygon;

	protected CalendarSection(DateStamp s) {
		start = s;
		angleStart = s.getAngle();
	}

	public abstract DateStamp getEnd();
	public abstract double getEndAngle();

}
