package Reika.GameCalendar.GUI;

import java.util.HashSet;
import java.util.List;

import Reika.GameCalendar.Data.ActivityCategory;

public interface CalendarItem {

	List<String> generateDescription();

	public String getDescriptiveDate();

	public HashSet<ActivityCategory> getActiveCategories();

}
