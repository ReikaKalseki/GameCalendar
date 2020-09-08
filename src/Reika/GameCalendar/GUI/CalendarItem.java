package Reika.GameCalendar.GUI;

import java.util.HashSet;
import java.util.List;

import Reika.GameCalendar.Data.ActivityCategory;

import javafx.scene.image.Image;

public interface CalendarItem {

	List<String> generateDescription();

	public String getDescriptiveDate();

	public HashSet<ActivityCategory> getActiveCategories();

	public Image getScreenshot();

}
