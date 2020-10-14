package Reika.GameCalendar.VideoExport.Insets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Reika.GameCalendar.Data.ActivityValue;
import Reika.GameCalendar.Data.LineGraph;
import Reika.GameCalendar.IO.FileIO;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.VideoExport.VideoInset;
import Reika.GameCalendar.VideoExport.VideoRenderer;

public class EDCreditsBalance implements VideoInset {

	private static final int XPOS = VideoRenderer.CALENDAR_SIZE;//+2*VideoRenderer.SCREENSHOT_WIDTH;
	private static final int YPOS = 2*VideoRenderer.SCREENSHOT_HEIGHT;//120;
	private static final int WIDTH = 2*VideoRenderer.SCREENSHOT_WIDTH;//VideoRenderer.VIDEO_WIDTH-XPOS;
	private static final int HEIGHT = 2*VideoRenderer.SCREENSHOT_HEIGHT;//400;

	private final LineGraph graphBalance = new LineGraph();
	private final LineGraph graphAssets = new LineGraph();
	private final LineGraph graphTotal = new LineGraph();

	private final ActivityValue activity;

	private final long limitValue;

	public EDCreditsBalance(File f, ActivityValue a) throws IOException {
		if (f.exists()) {
			ArrayList<String> li = FileIO.getFileAsLines(f);
			for (String s : li) {
				try {
					String[] parts = s.split(",");
					DateStamp date = DateStamp.parse(parts[0]);
					int off = parts[1].indexOf(':') >= 0 ? 1 : 0;
					long balance = Long.parseLong(parts[1+off]);
					long assets = Long.parseLong(parts[2+off]);
					long total = parts[3+off].isEmpty() ? -1 : Long.parseLong(parts[3+off]);
					graphBalance.addPoint(date, balance);
					graphAssets.addPoint(date, assets);
					if (total >= 0)
						graphTotal.addPoint(date, total);
				}
				catch (Exception e) {
					throw new IllegalArgumentException("Invalid CSV line '"+s+"'", e);
				}
			}
			graphBalance.calculate();
			graphAssets.calculate();
			graphTotal.calculate();
		}
		activity = a;
		limitValue = (long)Math.max(Math.max(graphAssets.getMaxValue(), graphBalance.getMaxValue()), graphTotal.getMaxValue());
	}

	@Override
	public void draw(BufferedImage frame, Graphics2D g, Font f, DateStamp date) {
		this.drawLines(date, g, graphBalance, Color.red);
		this.drawLines(date, g, graphAssets, new Color(0, 170, 0));
		this.drawLines(date, g, graphTotal, Color.BLUE);
	}

	private void drawLines(DateStamp root, Graphics2D g, LineGraph line, Color c) {
		long bmain = this.getBalance(root, line);
		if (bmain < 0)
			return;

		g.setColor(c);
		int xctr = XPOS+WIDTH;
		int yctr = YPOS+HEIGHT;
		int widthPerDay = 2;

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
		}
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

	private int getHeight(long val) {
		return (int)(val*HEIGHT/limitValue);
	}

	private long getBalance(DateStamp date, LineGraph gr) {
		long val = (long)gr.getValueAt(date);
		if (!activity.isActiveAt(date)) {
			DateStamp last = activity.getLastActiveDateBefore(date);
			if (last == null)
				return -1;
			val = (long)gr.getValueAt(last);
		}
		return val;
	}

	/*
	@Override
	public boolean shouldRegister() {

	}
	 */
}
