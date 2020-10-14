package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.MathHelper;

public class LineGraph {

	private final HashMap<DateStamp, ArrayList<Double>> raw = new HashMap();
	private final TreeMap<DateStamp, Double> data = new TreeMap();

	private double maxValue;

	public void addPoint(DateStamp date, double value) {
		ArrayList<Double> li = raw.get(date);
		if (li == null) {
			li = new ArrayList();
			raw.put(date, li);
		}
		li.add(value);
	}

	public void calculate() {
		for (Entry<DateStamp, ArrayList<Double>> e : raw.entrySet()) {
			ArrayList<Double> li = e.getValue();
			double sum = 0;
			for (double d : li) {
				sum += d;
			}
			sum /= li.size();
			data.put(e.getKey(), sum);
			maxValue = Math.max(sum, maxValue);
		}
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public double getValueAt(DateStamp date) {
		Entry<DateStamp, Double> key = data.floorEntry(date);
		return key != null ? key.getValue() : 0;
	}

	public DateStamp getPointBefore(DateStamp date) {
		DateStamp ret = data.floorKey(date);
		if (ret != null && ret.equals(date)) {
			ret = data.lowerKey(ret);
		}
		return ret;
	}

	public TreeMap<DateStamp, Double> unroll(ActivityValue av) {
		TreeMap<DateStamp, Double> ret = new TreeMap();
		DateStamp start = data.firstKey();
		DateStamp at = start;
		DateStamp next = data.higherKey(at);
		while (next != null) {
			while (!at.equals(next)) {
				Double val = data.get(at);
				if (val == null) {
					Entry<DateStamp, Double> e1 = data.floorEntry(at);
					Entry<DateStamp, Double> e2 = data.ceilingEntry(at);
					if (av.areSeparated(e1.getKey(), e2.getKey())) {
						val = e1.getValue();
					}
					else {
						int step = e1.getKey().countDaysAfter(e2.getKey());
						double y1 = e1.getValue();
						double y2 = e2.getValue();
						val = MathHelper.linterpolate(e1.getKey().countDaysAfter(at), 0, step, y1, y2);
					}
				}
				ret.put(at, val);
				at = at.nextDay();
			}
			at = next;
			next = data.higherKey(next);
		}
		return ret;
	}

	@Override
	public String toString() {
		//return data.toString();
		StringBuilder sb = new StringBuilder();
		for (Entry<DateStamp, Double> e : data.entrySet()) {
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

	public double getMaxValue() {
		return maxValue;
	}

}
