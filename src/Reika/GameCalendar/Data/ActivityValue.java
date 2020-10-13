package Reika.GameCalendar.Data;

import java.util.Map.Entry;
import java.util.TreeMap;

import Reika.GameCalendar.Util.DateStamp;

public class ActivityValue {

	private final TreeMap<DateStamp, Integer> data = new TreeMap();
	//private final ArrayList<TimeSpan> activePeriods = new ArrayList();

	public void addPoint(DateStamp date, int value) {
		data.put(date, value);
	}

	public int getValueAt(DateStamp date) {
		Entry<DateStamp, Integer> key = data.floorEntry(date);
		return key != null ? key.getValue() : 0;
	}

	public boolean isActiveAt(DateStamp date) {
		return this.getValueAt(date) > 0;
	}

	public DateStamp getLastActiveDateBefore(DateStamp ref) {
		Entry<DateStamp, Integer> key = data.floorEntry(ref);
		if (key == null)
			return null;
		if (key.getValue() > 0)
			return ref;
		DateStamp prev = key.getKey();
		while (key != null && key.getValue() == 0) {
			key = data.lowerEntry(key.getKey());
		}
		if (key == null)
			return null;
		DateStamp startOf = key.getKey();
		return data.higherKey(startOf).previousDay();
	}

	public DateStamp getStartOfPeriod(DateStamp date) {
		return data.floorKey(date);
	}

	public DateStamp getEndOfPeriod(DateStamp date) {
		return data.ceilingKey(date);
	}

	@Override
	public String toString() {
		//return data.toString();
		StringBuilder sb = new StringBuilder();
		for (Entry<DateStamp, Integer> e : data.entrySet()) {
			sb.append("[");
			sb.append(e.getKey());
			sb.append("-");
			DateStamp next = data.higherKey(e.getKey());
			if (next != null) {
				sb.append(next);
			}
			sb.append("]=");
			sb.append(e.getValue());
			sb.append("; ");
		}
		return sb.toString();
	}

}
