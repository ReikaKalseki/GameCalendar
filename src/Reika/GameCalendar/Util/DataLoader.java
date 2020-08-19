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

	public static Timeline loadTimeline(File folder) throws IOException {
		Timeline ret = new Timeline();
		File events = new File(folder, "Events");
		for (File f : events.listFiles()) {
			if (f.length() == 0) {
				System.out.println("Event File '"+f.getName()+"' is empty!");
				continue;
			}
			try {
				ret.addEvent(parseEvent(f));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+f.getName()+"' is invalid.", e);
			}
		}

		File ranges = new File(folder, "Periods");
		for (File f : ranges.listFiles()) {
			if (f.length() == 0) {
				System.out.println("Period File '"+f.getName()+"' is empty!");
				continue;
			}
			try {
				ret.addPeriod(parsePeriod(f));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("File '"+f.getName()+"' is invalid.", e);
			}
		}

		return ret;
	}

	private static Highlight parseEvent(File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new Highlight(ActivityCategory.getByName(map.get("category")), DateStamp.parse(map.get("date")), map.get("name"), map.get("desc"));
	}

	private static TimeSpan parsePeriod(File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new TimeSpan(ActivityCategory.getByName(map.get("category")), DateStamp.parse(map.get("start")), DateStamp.parse(map.get("end")), map.get("name"), map.get("desc"));
	}

	public static HashMap<String, String> getFileData(File f) throws IOException {
		HashMap<String, String> ret = new HashMap();
		ArrayList<String> li = FileIO.getFileAsLines(f);
		for (String s : li) {
			String[] parts = s.split(":");
			if (parts.length == 2)
				ret.put(parts[0], parts[1]);
			else
				System.out.println("File '"+f.getName()+"' has valueless keys!");
		}
		String n = f.getName().substring(0, f.getName().length()-4);
		ret.put("name", n);
		return ret;
	}

}
