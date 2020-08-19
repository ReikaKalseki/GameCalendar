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
	/*
	DO NOT FORGET:
		KSP (NOT IN SHAREX FILES due to copy-upload)
		ELITE SESSIONS
		FACTORIO SESSIONS
		PORTAL MAPS & PETI PLAYTHROUGHS
		PSMEL
		SPACE ENGINEERS (CHECK STEAM AND DEDICATED SCREENSHOTS)
		2014 EVERYTHING
		CIV PLAYTHROUGHS (NOT SHAREX)*/

	public static void loadTimeline(Timeline ret, ActivityCategory cat) throws IOException {
		File events = new File(cat.folder, "Events");
		for (File f : events.listFiles()) {
			if (f.length() == 0) {
				System.out.println("Event File '"+f.getName()+"' is empty!");
				continue;
			}
			try {
				ret.addEvent(parseEvent(cat, f));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+f.getName()+"' is invalid.", e);
			}
		}

		File ranges = new File(cat.folder, "Periods");
		for (File f : ranges.listFiles()) {
			if (f.length() == 0) {
				System.out.println("Period File '"+f.getName()+"' is empty!");
				continue;
			}
			try {
				ret.addPeriod(parsePeriod(cat, f));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+f.getName()+"' is invalid.", e);
			}
		}
	}

	private static Highlight parseEvent(ActivityCategory a, File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new Highlight(a, DateStamp.parse(map.get("date")), map.get("name"), map.get("desc"));
	}

	private static TimeSpan parsePeriod(ActivityCategory a, File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new TimeSpan(a, DateStamp.parse(map.get("start")), DateStamp.parse(map.get("end")), map.get("name"), map.get("desc"));
	}

	public static HashMap<String, String> getFileData(File f) throws IOException {
		HashMap<String, String> ret = new HashMap();
		ArrayList<String> li = FileIO.getFileAsLines(f);
		for (String s : li) {
			int idx = s.indexOf(':');
			if (idx < 0) {
				System.out.println("File '"+f.getName()+"' has invalid line '"+s+"'!");
			}
			else {
				if (idx > 0 && idx < s.length()-1)
					ret.put(s.substring(0, idx), s.substring(idx+1));
				else
					System.out.println("File '"+f.getName()+"' has valueless line '"+s+"'!");
			}
		}
		String n = f.getName().substring(0, f.getName().length()-4);
		ret.put("name", n);
		return ret;
	}

}
