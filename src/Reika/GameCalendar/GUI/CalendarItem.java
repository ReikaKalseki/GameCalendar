package Reika.GameCalendar.GUI;

import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.CalendarEvent;

public interface CalendarItem {

	//List<String> generateDescription();

	public String getDescriptiveDate();

	public Set<ActivityCategory> getActiveCategories();

	public List<? extends CalendarEvent> getItems(boolean activeOnly);

	public boolean containsYear(int year, boolean activeOnly);

}
