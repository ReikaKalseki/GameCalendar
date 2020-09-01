package Reika.GameCalendar.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Util.DataLoader;

public class ActivityCategory implements Comparable<ActivityCategory> {

	private static final HashMap<String, ActivityCategory> categories = new HashMap();

	public static ActivityCategory getByName(String s) {
		return categories.get(s);
	}

	public final String name;
	public final String desc;
	public final int color;

	public final int sortingIndex;

	public final File folder;

	private ActivityCategory(File f, String n, String d, int c, int idx) {
		name = n;
		desc = d;
		color = c;
		sortingIndex = idx;
		folder = f;
	}

	public static void loadCategory(File in, Timeline t) throws Exception {
		HashMap<String, String> data = DataLoader.getFileData(in.getName(), new File(in, "info.txt"));
		int idx = data.containsKey("index") ? Integer.parseInt(data.get("index")) : 0;
		ActivityCategory a = new ActivityCategory(in, in.getName(), data.get("desc"), Integer.parseInt(data.get("color"), 16), idx);
		a.loadFiles(t);
		categories.put(a.name, a);
	}

	private void loadFiles(Timeline t) throws Exception {
		DataLoader.loadTimeline(t, this);
	}

	public static Set<String> getNameList() {
		return Collections.unmodifiableSet(categories.keySet());
	}

	public static List<String> getSortedNameList(SortingMode mode) {
		return mode.getSortedList(categories.values());
	}

	public static List<String> getAlphaSortedNameList() {
	}

	public static enum SortingMode {
		ALPHA("Alphabetical"),
		EXPLICIT("File-specified"),
		TIME("By first appearance");

		private final String displayName;

		private SortingMode(String s) {
			displayName = s;
		}

		private List<String> getSortedList(Collection<ActivityCategory> c) {
			ArrayList<String> li = new ArrayList();
			switch(this) {
				case ALPHA:
					ArrayList<String> li = new ArrayList(categories.keySet());
					Collections.sort(li, String.CASE_INSENSITIVE_ORDER);
					return li;
				case EXPLICIT:
					ArrayList li = new ArrayList(categories.values());
					Collections.sort(li);
					for (int i = 0; i < li.size(); i++) {
						ActivityCategory cat = (ActivityCategory)li.get(i);
						li.set(i, cat.name);
					}
					return li;
				case TIME:
					break;
				default:
					break;
			}
		}

		public static List<String> list() {
			ArrayList<String> li = new ArrayList();
			for (SortingMode m : values()) {
				li.add(m.displayName);
			}
			return li;
		}
	}

}
