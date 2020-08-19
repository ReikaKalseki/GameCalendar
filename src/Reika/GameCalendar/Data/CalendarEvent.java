package Reika.GameCalendar.Data;

import Reika.GameCalendar.Util.DateStamp;

public abstract class CalendarEvent {

	public final String name;
	public final String description;

	public final ActivityCategory category;

	public CalendarEvent(ActivityCategory a, String n, String desc) {
		if (a == null)
			throw new IllegalArgumentException("Null category for '"+n+"'!");
		category = a;
		name = n;
		description = desc;
	}

	@Override
	public final String toString() {
		return name+" @ "+this.getDescriptiveDate().toString();
	}

	protected abstract DateStamp getDescriptiveDate();

	public abstract int getColor();

}
