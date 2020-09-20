package Reika.GameCalendar.Data;

import java.util.List;

public interface CompoundElement<A extends CalendarEvent> {

	public List<A> getElements();

}
