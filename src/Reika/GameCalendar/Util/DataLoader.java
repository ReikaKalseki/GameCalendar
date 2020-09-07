package Reika.GameCalendar.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.Data.Timeline;

public class DataLoader {

	public static DateStamp loadTimeline(Timeline ret, ActivityCategory cat) throws IOException {
		DateStamp first = null;
		File events = new File(cat.folder, "Events");
		for (File f : events.listFiles()) {
			if (f.length() == 0) {
				System.err.println("Event File '"+cat.folder.getName()+"/"+f.getName()+"' is empty!");
				continue;
			}
			try {
				Highlight h = parseEvent(cat, f);
				ret.addEvent(h);
				if (first == null || h.time.compareTo(first) < 0)
					first = h.time;
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+cat.folder.getName()+"/"+f.getName()+"' is invalid.", e);
			}
		}

		File ranges = new File(cat.folder, "Periods");
		for (File f : ranges.listFiles()) {
			if (f.length() == 0) {
				System.err.println("Period File '"+cat.folder.getName()+"/"+f.getName()+"' is empty!");
				continue;
			}
			try {
				TimeSpan ts = parsePeriod(cat, f);
				ret.addPeriod(ts);
				if (first == null || ts.start.compareTo(first) < 0)
					first = ts.start;
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+cat.folder.getName()+"/"+f.getName()+"' is invalid.", e);
			}
		}
		if (first == null)
			first = ret.getEnd();
		return first;
	}

	private static Highlight parseEvent(ActivityCategory a, File f) throws IOException {
		HashMap<String, String> map = getFileData(a.folder.getName(), f);
		Highlight ret = new Highlight(a, DateStamp.parse(map.get("date")), map.get("name"), map.get("desc"));
		if (map.containsKey("screenshot")) {
			ret.setScreenshot(new File(map.get("screenshot")));
		}
		return ret;
	}

	private static TimeSpan parsePeriod(ActivityCategory a, File f) throws IOException {
		HashMap<String, String> map = getFileData(a.folder.getName(), f);
		TimeSpan ret = new TimeSpan(a, DateStamp.parse(map.get("start")), DateStamp.parse(map.get("end")), map.get("name"), map.get("desc"));
		if (map.containsKey("screenshot")) {
			ret.setScreenshot(new File(map.get("screenshot")));
		}
		return ret;
	}

	public static HashMap<String, String> getFileData(String cat, File f) throws IOException {
		HashMap<String, String> ret = new HashMap();
		ArrayList<String> li = FileIO.getFileAsLines(f);
		for (String s : li) {
			int idx0 = s.indexOf('$');
			if (idx0 > 0) {
				s = s.substring(0, idx0);
			}
			int idx = s.indexOf(':');
			if (idx < 0) {
				System.err.println("File '"+cat+"/"+f.getName()+"' has invalid line '"+s+"'!");
			}
			else {
				if (idx > 0 && idx < s.length()-1)
					ret.put(s.substring(0, idx), s.substring(idx+1));
				else
					System.err.println("File '"+cat+"/"+f.getName()+"' has valueless line '"+s+"'!");
			}
		}
		String n = f.getName().substring(0, f.getName().length()-4);
		ret.put("name", n);
		return ret;
	}

}
