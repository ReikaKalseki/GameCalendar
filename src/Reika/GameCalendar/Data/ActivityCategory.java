package Reika.GameCalendar.Data;

import java.io.File;
import java.util.HashMap;

import Reika.GameCalendar.Util.DataLoader;

public class ActivityCategory {

	private static final HashMap<String, ActivityCategory> categories = new HashMap();

	public static ActivityCategory getByName(String s) {
		return categories.get(s);
	}

	public final String name;
	public final String desc;
	public final int color;

	public final File folder;

	private ActivityCategory(File f, String n, String d, int c) {
		name = n;
		desc = d;
		color = c;
		folder = f;
	}

	public static void loadCategory(File in, Timeline t) throws Exception {
		HashMap<String, String> data = DataLoader.getFileData(new File(in, "info.txt"));
		ActivityCategory a = new ActivityCategory(in, in.getName(), data.get("desc"), Integer.parseInt(data.get("color"), 16));
		a.loadFiles(t);
		categories.put(a.name, a);
	}

	private void loadFiles(Timeline t) throws Exception {
		DataLoader.loadTimeline(t, this);
	}

}
