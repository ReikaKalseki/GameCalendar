package Reika.GameCalendar.GUI;

import java.util.ArrayList;
import java.util.List;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.Util.DoublePoint;

public class GuiHighlight implements CalendarItem {

	public final Highlight event;

	public DoublePoint position;

	public GuiHighlight(Highlight h) {
		event = h;
	}

	@Override
	public List<String> generateDescription() {
		ArrayList<String> ret = new ArrayList();
		String line = event.category.name+": "+event.name+" ["+event.time+"]";
		ret.add(line);
		if (!Strings.isNullOrEmpty(event.description)) {
			ret.add("\t"+event.description);
		}
		return ret;
	}

	@Override
	public String getDescriptiveDate() {
		return event.time.toString();
	}

}
