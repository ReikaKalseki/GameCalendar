package Reika.GameCalendar.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.IO.DataLoader;
import Reika.GameCalendar.Util.DateStamp;

public class ActivityCategory implements Comparable<ActivityCategory> {

	private static final HashMap<String, ActivityCategory> categories = new HashMap();

	private static SortingMode sorting = SortingMode.TIME;

	public static ActivityCategory getByName(String s) {
		return categories.get(s);
	}

	private final HashMap<String, String> data;

	public final String name;
	public final String desc;
	public final int color;

	public final int sortingIndex;

	public final File folder;

	private DateStamp firstDate;

	private ActivityCategory(File f, String n, HashMap<String, String> map, int c, int idx) {
		data = new HashMap(map);
		name = n;
		desc = data.get("desc");
		color = c;
		sortingIndex = idx;
		folder = f;
	}

	public static void loadCategory(File in, Timeline t) throws Exception {
		HashMap<String, String> data = DataLoader.getFileData(in.getName(), new File(in, "info.txt"));
		String name = data.get("display");
		if (name == null) {
			name = in.getName();
		}
		int idx = data.containsKey("index") ? Integer.parseInt(data.get("index")) : 0;
		ActivityCategory a = new ActivityCategory(in, name, data, Integer.parseInt(data.get("color"), 16), idx);
		a.loadFiles(t);
		categories.put(a.name, a);
	}

	private void loadFiles(Timeline t) throws Exception {
		firstDate = DataLoader.loadTimeline(t, this);
	}

	public static Set<String> getNameList() {
		return Collections.unmodifiableSet(categories.keySet());
	}

	public static Collection<ActivityCategory> getAllCategories() {
		return Collections.unmodifiableCollection(categories.values());
	}

	public static List<String> getSortedNameList(SortingMode mode) {
		return mode.getSortedList(categories.values(), Main.getTimeline());
	}

	public static boolean areAllCategoriesActive() {
		for (String s : ActivityCategory.getNameList()) {
			if (!GuiElement.CATEGORIES.isStringSelected(s))
				return false;
		}
		return true;
	}

	public static HashSet<ActivityCategory> getActiveCategories() {
		HashSet<ActivityCategory> ret = new HashSet();
		for (String s : ActivityCategory.getNameList()) {
			if (GuiElement.CATEGORIES.isStringSelected(s))
				ret.add(ActivityCategory.getByName(s));
		}
		return ret;
	}

	@Override
	public String toString() {
		return name+" = "+desc;
	}

	public static enum SortingMode {
		ALPHA("By Category - Alphabetical"),
		EXPLICIT("By Category - File-Specified"),
		TIME("By Category - First Appearance"),
		INDIVIDUAL("By Individual Date");

		private final String displayName;

		private SortingMode(String s) {
			displayName = s;
		}

		private List<String> getSortedList(Collection<ActivityCategory> data, Timeline t) {
			ArrayList<String> ret = new ArrayList();
			sorting = this;
			ArrayList<ActivityCategory> li = new ArrayList(data);
			Collections.sort(li);
			sorting = null;
			for (ActivityCategory cat : li) {
				ret.add(cat.name);
			}
			return ret;
		}

		public static List<String> list() {
			ArrayList<String> li = new ArrayList();
			for (SortingMode m : values()) {
				li.add(m.displayName);
			}
			return li;
		}
	}

	@Override
	public int compareTo(ActivityCategory o) {
		return this.compareTo(sorting, o);
	}

	public int compareTo(SortingMode mode, ActivityCategory o) {
		switch(mode) {
			case ALPHA:
				return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
			case EXPLICIT:
				return Integer.compare(sortingIndex, o.sortingIndex);
			case TIME:
			case INDIVIDUAL:
				return firstDate.compareTo(o.firstDate);
			default:
				return 0;
		}
	}

	@Override
	public final int hashCode() {
		return name.hashCode();
	}

	@Override
	public final boolean equals(Object o) {
		return o instanceof ActivityCategory && ((ActivityCategory)o).name.equals(name);
	}

	public HashMap<String, String> provideDataOverrides() {
		HashMap<String, String> ret = new HashMap();
		String over = data.get("override");
		if (over != null) {
			String[] parts = over.split(",");
			for (String s : parts) {
				String[] kv = s.split("=");
				if (kv.length != 2) {
					System.err.println("Invalid override data '"+over+"' in "+name);
					return ret;
				}
				ret.put(kv[0], kv[1]);
			}
		}
		return ret;
	}

}
