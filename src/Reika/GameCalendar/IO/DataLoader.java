package Reika.GameCalendar.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.Util.DateStamp;

public class DataLoader {

	public static DateStamp loadTimeline(Timeline ret, ActivityCategory cat) throws IOException {
		DateStamp first = null;
		File events = new File(cat.folder, "Events");
		for (File f : events.listFiles()) {
			if (f.length() == 0) {
				System.err.println("Event File '"+cat.folder.getName()+"/"+f.getName()+"' is empty!");
				continue;
			}
			if (!"text/plain".equals(Files.probeContentType(f.toPath()))) {
				System.out.println("Skipping non-text file '"+cat.folder.getName()+"/"+f.getName()+"'");
				continue;
			}
			if (f.getName().contains(cat.name)) {
				System.err.println("File '"+f+"' has redundant naming!");
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
			if (!"text/plain".equals(Files.probeContentType(f.toPath()))) {
				System.out.println("Skipping non-text file '"+cat.folder.getName()+"/"+f.getName()+"'");
				continue;
			}
			if (f.getName().contains(cat.name)) {
				System.err.println("File '"+f+"' has redundant naming!");
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
		Highlight ret = new Highlight(f, map, a, DateStamp.parse(map.get("date")));
		if (map.containsKey("screenshot")) {
			ret.setScreenshot(new File(map.get("screenshot")));
		}
		if (Boolean.valueOf(map.get("memorable"))) {
			ret.setMemorable();
		}
		String priv = map.get("privacy");
		if (priv != null) {
			ret.setPrivacy(Integer.parseInt(priv));
		}
		return ret;
	}

	private static TimeSpan parsePeriod(ActivityCategory a, File f) throws IOException {
		HashMap<String, String> map = getFileData(a.folder.getName(), f);
		TimeSpan ret = new TimeSpan(f, map, a, DateStamp.parse(map.get("start")), DateStamp.parse(map.get("end")));
		if (map.containsKey("screenshot")) {
			ret.setScreenshot(new File(map.get("screenshot")));
		}
		if (Boolean.valueOf(map.get("memorable"))) {
			ret.setMemorable();
		}
		String priv = map.get("privacy");
		if (priv != null) {
			ret.setPrivacy(Integer.parseInt(priv));
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
		String oops = ret.get("screenshots");
		if (oops != null) {
			System.err.println("File "+f.getAbsolutePath()+" contains a typo: 'screenshots' instead of 'screenshot'!");
			ret.put("screenshot", oops);
		}
		return ret;
	}

}
