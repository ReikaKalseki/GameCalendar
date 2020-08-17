package Reika.GameCalendar.Data;

import Reika.GameCalendar.Util.DateStamp;

public abstract class CalendarEvent {

	public final String name;
	public final String description;

	public CalendarEvent(String n, String desc) {
		name = n;
		description = desc;
	}

	@Override
	public final String toString() {
		return name+" @ "+this.getDescriptiveDate().toString();
	}

	protected abstract DateStamp getDescriptiveDate();

}
