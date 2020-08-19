package Reika.GameCalendar.Data;

import java.io.File;
import java.io.IOException;
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

	private ActivityCategory(String n, String d, int c) {
		name = n;
		desc = d;
		color = c;
	}

	public static void loadCategories(File f) throws IOException {
		for (File in : f.listFiles()) {
			if (in.isDirectory())
				loadCategories(in);
			else {
				try {
					loadCategory(in);
				}
				catch (Exception e) {
					throw new RuntimeException("Could not load category file '"+in.getName()+"'", e);
				}
			}
		}
	}

	private static void loadCategory(File in) throws Exception {
		HashMap<String, String> data = DataLoader.getFileData(in);
		ActivityCategory a = new ActivityCategory(data.get("name"), data.get("desc"), Integer.parseInt(data.get("color"), 16));
		categories.put(a.name, a);
	}

}
