package Reika.GameCalendar.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.MathHelper;

public class LineGraph {

	private final HashMap<DateStamp, ArrayList<Double>> raw = new HashMap();
	private final TreeMap<DateStamp, DayData> data = new TreeMap();

	private double maxValue;

	public void addPoint(DateStamp date, double value) {
		ArrayList<Double> li = raw.get(date);
		if (li == null) {
			li = new ArrayList();
			raw.put(date, li);
		}
		li.add(value);
	}

	public void calculate(int maxPerDay) {
		for (Entry<DateStamp, ArrayList<Double>> e : raw.entrySet()) {
			DayData dd = new DayData(e.getValue(), maxPerDay);
			data.put(e.getKey(), dd);
			maxValue = Math.max(dd.highestValue, maxValue);
		}
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public double getAverageValueAt(DateStamp date) {
		Entry<DateStamp, DayData> key = data.floorEntry(date);
		return key != null ? key.getValue().averageValue : 0;
	}

	public DateStamp getPointBefore(DateStamp date) {
		DateStamp ret = data.floorKey(date);
		if (ret != null && ret.equals(date)) {
			ret = data.lowerKey(ret);
		}
		return ret;
	}

	public TreeMap<DateStamp, ArrayList<Double>> unroll(int maxPerDay) {
		TreeMap<DateStamp, ArrayList<Double>> ret = new TreeMap();
		DateStamp start = data.firstKey();
		DateStamp at = start;
		DateStamp next = data.higherKey(at);
		while (next != null) {
			while (!at.equals(next)) {
				DayData val = data.get(at);
				ArrayList<Double> li = new ArrayList();
				if (val == null) {
					Entry<DateStamp, DayData> e1 = data.floorEntry(at);
					Entry<DateStamp, DayData> e2 = data.ceilingEntry(at);
					int step = e1.getKey().countDaysAfter(e2.getKey());
					double y1 = e1.getValue().getLastValue();
					double y2 = e2.getValue().getFirstValue();
					li.add(MathHelper.linterpolate(e1.getKey().countDaysAfter(at), 0, step, y1, y2));
				}
				else {
					li.addAll(val.dataSeries);
				}
				ret.put(at, li);
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
		for (Entry<DateStamp, DayData> e : data.entrySet()) {
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

	private static class DayData {

		private final ArrayList<Double> dataSeries = new ArrayList();
		private final double averageValue;
		private final double highestValue;

		private DayData(ArrayList<Double> raw, int nsteps) {
			double sum = 0;
			double max = Double.NEGATIVE_INFINITY;
			for (double d : raw) {
				sum += d;
				max = Math.max(max, d);
			}
			sum /= raw.size();
			averageValue = sum;
			highestValue = max;

			if (nsteps >= raw.size()) {
				dataSeries.addAll(raw);
			}
			else if (nsteps == 1) {
				dataSeries.add(averageValue);
			}
			else {
				int nstepsOrig = raw.size()-1;
				double dStepOrig = 1D/nstepsOrig;
				double dStepNew = 1D/nsteps;
				for (int i = 0; i < nsteps; i++) {
					double fracSeek = i*dStepNew+dStepNew/2;
					double stepProgress = fracSeek/dStepOrig;
					double lowerFrac = MathHelper.roundDownToFraction(fracSeek, dStepOrig);
					double higherFrac = MathHelper.roundUpToFraction(fracSeek, dStepOrig);
					int lowerIdx = (int)(nstepsOrig*lowerFrac);
					int higherIdx = (int)(nstepsOrig*higherFrac);
					double lowerVal = raw.get(lowerIdx);
					double higherVal = raw.get(higherIdx);
					double dVal = higherVal-lowerVal;
					double dY = stepProgress*dVal;
					double result = dY+lowerVal;
					dataSeries.add(result);
				}
			}
		}

		public double getFirstValue() {
			return dataSeries.get(0);
		}

		public double getLastValue() {
			return dataSeries.get(dataSeries.size()-1);
		}

		@Override
		public String toString() {
			return dataSeries.toString();
		}

	}

}
