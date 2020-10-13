package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import Reika.GameCalendar.Util.DateStamp;

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
