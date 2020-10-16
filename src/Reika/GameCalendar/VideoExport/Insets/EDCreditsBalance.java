package Reika.GameCalendar.VideoExport.Insets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

import Reika.GameCalendar.Data.ActivityValue;
import Reika.GameCalendar.Data.LineGraph;
import Reika.GameCalendar.IO.FileIO;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.MathHelper;
import Reika.GameCalendar.VideoExport.VideoInset;
import Reika.GameCalendar.VideoExport.VideoRenderer;

public class EDCreditsBalance implements VideoInset {

	private static final int PAD = 20;
	private static final int XPOS = VideoRenderer.CALENDAR_SIZE;//+2*VideoRenderer.SCREENSHOT_WIDTH;
	private static final int YPOS = 2*VideoRenderer.SCREENSHOT_HEIGHT+PAD;//120;
	private static final int WIDTH = VideoRenderer.VIDEO_WIDTH-VideoRenderer.CALENDAR_SIZE-PAD;//2*VideoRenderer.SCREENSHOT_WIDTH;//VideoRenderer.VIDEO_WIDTH-XPOS;
	private static final int HEIGHT = 2*VideoRenderer.SCREENSHOT_HEIGHT-PAD*2;//400;
	private static final int AXIS_WIDTH = WIDTH-2*VideoRenderer.SCREENSHOT_WIDTH;
	private static final int AXIS_HEIGHT = VideoRenderer.SCREENSHOT_HEIGHT/12;
	private static final int WIDTH_PER_DAY = 5;//2;
	private static final int POINTS_PER_DAY = WIDTH_PER_DAY;
	private static double LOG_EXPONENT = 1;//2;
	private static boolean SLIDING_SCALE = true;
	private static final int SLIDING_SCALE_ROUND = 10000000;

	private final LineGraph graphBalance = new LineGraph();
	private final LineGraph graphAssets = new LineGraph();
	private final LineGraph graphTotal = new LineGraph();

	private final TreeMap<DateStamp, ArrayList<Double>> lineBalance;
	private final TreeMap<DateStamp, ArrayList<Double>> lineAssets;
	private final TreeMap<DateStamp, ArrayList<Double>> lineTotal;

	private final ActivityValue activity;

	private final long limitValue;
	private final long minValue;

	private long minRenderedThisFrame;
	private long maxRenderedThisFrame;
	private long axisLowerBoundThisFrame;
	private long axisUpperBoundThisFrame;

	public EDCreditsBalance(File f, ActivityValue a) throws IOException {
		if (f.exists()) {
			ArrayList<String> li = FileIO.getFileAsLines(f);
			for (String s : li) {
				try {
					String[] parts = s.split(",");
					int idx = parts[2].indexOf('@');
					if (idx >= 0) {
						parts[2] = parts[2].substring(0, idx-1);
					}
					DateStamp date = DateStamp.parse(parts[2]);
					long balance = parts[3].isEmpty() ? -1 : Long.parseLong(parts[3]);
					long assets = parts[4].isEmpty() ? -1 : Long.parseLong(parts[4]);
					long total = parts[5].isEmpty() ? -1 : Long.parseLong(parts[5]);
					if (balance >= 0)
						graphBalance.addPoint(date, balance);
					if (assets >= 0)
						graphAssets.addPoint(date, assets);
					if (total >= 0)
						graphTotal.addPoint(date, total);
				}
				catch (Exception e) {
					throw new IllegalArgumentException("Invalid CSV line '"+s+"'", e);
				}
			}
			graphBalance.calculate(POINTS_PER_DAY);
			graphAssets.calculate(POINTS_PER_DAY);
			graphTotal.calculate(POINTS_PER_DAY);
		}
		lineAssets = graphAssets.unroll(POINTS_PER_DAY);
		lineBalance = graphBalance.unroll(POINTS_PER_DAY);
		lineTotal = graphTotal.unroll(POINTS_PER_DAY);
		activity = a;
		limitValue = (long)Math.max(Math.max(graphAssets.getMaxValue(), graphBalance.getMaxValue()), graphTotal.getMaxValue());
		minValue = (long)Math.min(Math.min(graphAssets.getMinValue(), graphBalance.getMinValue()), graphTotal.getMinValue());
	}

	@Override
	public void draw(BufferedImage frame, Graphics2D g, Font f, DateStamp date) {
		//g.drawRect(XPOS, YPOS, WIDTH, HEIGHT);

		if (SLIDING_SCALE) {
			maxRenderedThisFrame = Long.MIN_VALUE;
			minRenderedThisFrame = Long.MAX_VALUE;
			axisUpperBoundThisFrame = Long.MIN_VALUE;
			axisLowerBoundThisFrame = Long.MAX_VALUE;
			this.calculateBounds(date);

			if (maxRenderedThisFrame < minRenderedThisFrame) {
				maxRenderedThisFrame = SLIDING_SCALE_ROUND;
				minRenderedThisFrame = 0;
			}
		}

		g.setStroke(new BasicStroke(5F));
		g.setColor(Color.black);
		g.drawLine(XPOS+AXIS_WIDTH, YPOS, XPOS+AXIS_WIDTH, YPOS+HEIGHT-AXIS_HEIGHT);
		g.drawLine(XPOS+AXIS_WIDTH, YPOS+HEIGHT-AXIS_HEIGHT, XPOS+WIDTH, YPOS+HEIGHT-AXIS_HEIGHT);

		g.setStroke(new BasicStroke(1F));
		g.setColor(Color.gray);
		if (LOG_EXPONENT == 1) {
			if (SLIDING_SCALE) {
				long min = Math.max(0, MathHelper.roundToNearestX(SLIDING_SCALE_ROUND, minRenderedThisFrame)-SLIDING_SCALE_ROUND);
				long max = MathHelper.roundToNearestX(SLIDING_SCALE_ROUND, maxRenderedThisFrame)+SLIDING_SCALE_ROUND;
				long averageThisFrame = (max+min)/2;
				long width = max-min;
				//axisLowerBoundThisFrame = (long)Math.pow(10, (int)Math.floor(Math.log10(minRenderedThisFrame)));
				//axisUpperBoundThisFrame = (long)Math.pow(10, (int)Math.ceil(Math.log10(maxRenderedThisFrame)));
				//NO - DO NOT ROUND, CAUSES JITTER
				axisLowerBoundThisFrame = Math.max(0, averageThisFrame-width);
				axisUpperBoundThisFrame = averageThisFrame+width;
				axisLowerBoundThisFrame = Math.max(0, MathHelper.roundToNearestX(SLIDING_SCALE_ROUND, axisLowerBoundThisFrame));
				axisUpperBoundThisFrame = MathHelper.roundToNearestX(SLIDING_SCALE_ROUND, axisUpperBoundThisFrame);

				long step = axisUpperBoundThisFrame/20;
				long value = axisLowerBoundThisFrame;
				while (value <= axisUpperBoundThisFrame) {
					int ly = YPOS+HEIGHT-AXIS_HEIGHT-this.getHeight(value);
					g.drawLine(XPOS+AXIS_WIDTH, ly, XPOS+WIDTH, ly);
					g.setColor(Color.black);
					String s = String.valueOf(value);
					g.drawString(s, XPOS+AXIS_WIDTH-g.getFontMetrics().stringWidth(s)-4, ly+f.getSize()/2);
					g.setColor(Color.gray);
					value += step;
				}
			}
			else {
				int gridStep = 20;
				for (int i = gridStep; i <= HEIGHT-AXIS_HEIGHT; i += gridStep) {
					int ly = YPOS+HEIGHT-AXIS_HEIGHT-i;
					g.drawLine(XPOS+AXIS_WIDTH, ly, XPOS+WIDTH, ly);
					g.setColor(Color.black);
					long axisVal = limitValue*i/HEIGHT;
					String s = String.valueOf(axisVal);
					g.drawString(s, XPOS+AXIS_WIDTH-g.getFontMetrics().stringWidth(s)-4, ly+f.getSize()/2);
					g.setColor(Color.gray);
				}
			}
		}
		else {
			double value = Math.pow(10, (int)Math.log10(minValue));//MathHelper.ceil2expLong(minValue)/2;
			int steps = (int)Math.ceil(Math.log(limitValue/minValue)/Math.log(LOG_EXPONENT));
			int gridStep = (HEIGHT-AXIS_HEIGHT)/steps;
			int ly = YPOS+HEIGHT-AXIS_HEIGHT-1;
			boolean flag = false;
			while (value <= limitValue || !flag) {
				flag |= value > limitValue;
				g.drawLine(XPOS+AXIS_WIDTH, ly, XPOS+WIDTH, ly);
				g.setColor(Color.black);
				String s = String.valueOf((long)(value));
				g.drawString(s, XPOS+AXIS_WIDTH-g.getFontMetrics().stringWidth(s)-4, ly+f.getSize()/2);
				g.setColor(Color.gray);
				value *= LOG_EXPONENT;
				ly -= gridStep;
			}
		}
		DateStamp at = date;
		int x = XPOS+WIDTH;
		while (x > XPOS+AXIS_WIDTH) {
			if (at.day == 1) {
				g.drawLine(x, YPOS, x, YPOS+HEIGHT-AXIS_HEIGHT);
				g.setColor(Color.black);
				String n = at.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())+" "+at.year;
				g.drawString(n, x-g.getFontMetrics().stringWidth(n)/2+WIDTH_PER_DAY*15, YPOS+HEIGHT-AXIS_HEIGHT*0/2);
				g.setColor(Color.gray);
			}
			at = at.previousDay();
			x -= WIDTH_PER_DAY;
		}

		g.setStroke(new BasicStroke(2F));
		this.drawLines(date, g, graphBalance, lineBalance, Color.red);
		this.drawLines(date, g, graphAssets, lineAssets, new Color(0, 170, 0));
		this.drawLines(date, g, graphTotal, lineTotal, Color.BLUE);
	}

	private void calculateBounds(DateStamp date) {
		this.drawLines(date, null, graphBalance, lineBalance, Color.red);
		this.drawLines(date, null, graphAssets, lineAssets, new Color(0, 170, 0));
		this.drawLines(date, null, graphTotal, lineTotal, Color.BLUE);
	}

	/** Null graphics to only calculate */
	private void drawLines(DateStamp root, Graphics2D g, LineGraph line, TreeMap<DateStamp, ArrayList<Double>> data, Color c) {
		/*
		long bmain = this.getBalance(root, line);
		if (bmain < 0)
			return;
		 */
		if (!data.containsKey(root))
			return;

		if (g != null)
			g.setColor(c);
		int xctr = XPOS+WIDTH;
		int yctr = YPOS+HEIGHT-AXIS_HEIGHT;

		/*
		//trying to fix a problem -> day-by-day brute force makes every increase occur in a single day, instead of as a line slope
		int x1 = xctr;
		DateStamp main = root;
		DateStamp prev = line.getPointBefore(root);
		DateStamp minDate = root.getOffset(0, -WIDTH/widthPerDay);
		while (prev != null && prev.compareTo(minDate) > 0) {
			bmain = this.getBalance(main, line);
			long bprev = this.getBalance(prev, line);
			int y1 = yctr-this.getHeight(bmain);
			int x2 = x1-widthPerDay*prev.countDaysAfter(main);
			int y2 = yctr-this.getHeight(bprev);
			g.drawLine(x1, y1, x2, y2);

			main = prev;
			prev = line.getPointBefore(main);
			x1 = x2;
		}
		if (!minDate.equals(main)) {
			bmain = this.getBalance(main, line);
			long bprev = this.getBalance(minDate, line);
			int x2 = XPOS;
			int y1 = yctr-this.getHeight(bmain);
			int y2 = yctr-this.getHeight(bprev);
			g.drawLine(x1, y1, x2, y2);
		}*/

		int x = xctr;
		DateStamp at = root;
		DateStamp prev = at.previousDay();
		while (data.containsKey(prev) && x >= XPOS+AXIS_WIDTH) {
			int x1 = x;
			int x2 = x1-WIDTH_PER_DAY;
			this.drawLinesForDay(g, data, x1, x2, yctr, at, prev, line);
			at = prev;
			x -= WIDTH_PER_DAY;
			prev = at.previousDay();
		}

		/*--------------------------------------------------------------------
		DateStamp main = root;
		DateStamp prev = main.previousDay();
		int x1 = xctr;
		int x2 = xctr-widthPerDay;
		long bprev = this.getBalance(prev, line);
		while (x2 >= XPOS) {
			int y1 = yctr-this.getHeight(this.getBalance(main, line));
			int y2 = yctr-this.getHeight(this.getBalance(prev, line));
			g.drawLine(x1, y1, x2, y2);

			main = prev;
			prev = main.previousDay();
			x1 -= widthPerDay;
			x2 -= widthPerDay;
		}---------------------------------------------------------------------
		 */

		/*
		DateStamp minDate = root.getOffset(0, -WIDTH/widthPerDay);
		int yVal = yctr-this.getHeight(this.getBalance(root, line));
		DateStamp prevPoint = line.getPointBefore(root);
		int x = xctr;
		int y = yVal;
		DateStamp lastDrawn = null;
		while (prevPoint != null && prevPoint.compareTo(minDate) > 0) {
			int x2 = x-widthPerDay;
			int y2 = yctr-this.getHeight(this.getBalance(prevPoint, line));
			g.drawLine(x, y, x2, y2);
			x = x2;
			y = y2;
			lastDrawn = prevPoint;
			prevPoint = line.getPointBefore(prevPoint);
		}
		if (lastDrawn == null || !lastDrawn.equals(minDate)) {
			int x2 = XPOS;
			int y2 = yctr-this.getHeight(this.getBalance(minDate, line));
			g.drawLine(x, y, x2, y2);
		}
		 */
	}

	private void drawLinesForDay(Graphics2D g, TreeMap<DateStamp, ArrayList<Double>> data, int xAt, int xPrev, int yctr, DateStamp at, DateStamp prev, LineGraph line) {
		long[] v1 = this.getDataAt(data, at);
		long[] v2 = this.getDataAt(data, prev);
		int width = xPrev-xAt;
		int dx = width/v1.length;
		int xAt0 = xAt-width/2;
		for (int i = 0; i < v1.length-1; i++) {
			if (g != null) {
				int y0 = yctr-this.getHeight(v1[i]);
				int y1 = yctr-this.getHeight(v1[i+1]);
				int x0 = xAt0+dx*i;
				int x1 = xAt0+dx*(i+1);
				g.drawLine(x0, y0, x1, y1);
			}

			maxRenderedThisFrame = Math.max(maxRenderedThisFrame, v1[i]);
			maxRenderedThisFrame = Math.max(maxRenderedThisFrame, v1[i+1]);
			minRenderedThisFrame = Math.min(minRenderedThisFrame, v1[i]);
			minRenderedThisFrame = Math.min(minRenderedThisFrame, v1[i+1]);
		}
		if (g != null) {
			int y1 = yctr-this.getHeight(v1[0]);
			int y2 = yctr-this.getHeight(v2[v2.length-1]);
			g.drawLine(xAt0, y1, xPrev+width/2-dx, y2);
		}

		maxRenderedThisFrame = Math.max(maxRenderedThisFrame, v2[v2.length-1]);
		minRenderedThisFrame = Math.min(minRenderedThisFrame, v2[v2.length-1]);
	}

	private long[] getDataAt(TreeMap<DateStamp, ArrayList<Double>> data, DateStamp date) {
		if (!activity.isActiveAt(date)) {
			ArrayList<Double> li = data.get(activity.getLastActiveDateBefore(date));
			return new long[] {li.get(li.size()-1).longValue()};
		}
		ArrayList<Double> li = data.get(date);
		long[] ret = new long[li.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = li.get(i).longValue();
		}
		return ret;
	}

	private int getHeight(double val) {
		double lim = limitValue;
		long d = 0;
		if (SLIDING_SCALE) {
			lim = axisUpperBoundThisFrame-axisLowerBoundThisFrame;
			d = axisLowerBoundThisFrame;
		}
		if (LOG_EXPONENT > 1 && val > 0) {
			val = val <= minValue ? 0 : (Math.log(val)-Math.log(minValue))/Math.log(LOG_EXPONENT);
			lim = Math.log(limitValue-minValue)/Math.log(LOG_EXPONENT);
		}
		int base = (int)((val-d)*(HEIGHT-AXIS_HEIGHT)/lim);
		/*
		if (LOG_EXPONENT > 1) {
	        double b = Math.log(y/x)/(y-x);
	        double a = this.limitValue / Math.exp(b*this.limitValue);
	       return (int)Math.round(a * Math.exp(b*val));
		}
		 */
		return base;
	}

	/*
	private long getBalance(DateStamp date, LineGraph gr) {
		long val = (long)gr.getValueAt(date);
		if (!activity.isActiveAt(date)) {
			DateStamp last = activity.getLastActiveDateBefore(date);
			if (last == null)
				return -1;
			val = (long)gr.getValueAt(last);
		}
		return val;
	}*/

	/*
	@Override
	public boolean shouldRegister() {

	}
	 */
}
