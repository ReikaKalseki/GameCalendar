package Reika.GameCalendar.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

	public static Timeline loadTimeline(String path) throws IOException {
		Timeline ret = new Timeline();
		File folder = new File(path);
		File events = new File(folder, "Events");
		for (File f : events.listFiles()) {
			if (f.length() == 0) {
				System.out.println("File '"+f.getName()+"' is empty!");
				continue;
			}
			ret.addEvent(parseEvent(f));
		}

		File ranges = new File(folder, "Periods");
		for (File f : ranges.listFiles()) {
			if (f.length() == 0) {
				System.out.println("File '"+f.getName()+"' is empty!");
				continue;
			}
			ret.addPeriod(parsePeriod(f));
		}

		return ret;
	}

	private static Highlight parseEvent(File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new Highlight(DateStamp.parse(map.get("date")), map.get("name"), map.get("desc"));
	}

	private static TimeSpan parsePeriod(File f) throws IOException {
		HashMap<String, String> map = getFileData(f);
		return new TimeSpan(DateStamp.parse(map.get("start")), DateStamp.parse(map.get("end")), map.get("name"), map.get("desc"));
	}

	private static HashMap<String, String> getFileData(File f) throws IOException {
		HashMap<String, String> ret = new HashMap();
		ArrayList<String> li = FileIO.getFileAsLines(f);
		for (String s : li) {
			String[] parts = s.split(":");
			ret.put(parts[0], parts[1]);
		}
		String n = f.getName().substring(0, f.getName().length()-4);
		ret.put("name", n);
		return ret;
	}

}
