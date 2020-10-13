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

	private static final int XPOS = VideoRenderer.CALENDAR_SIZE+2*VideoRenderer.SCREENSHOT_WIDTH;
	private static final int YPOS = 120;
	private static final int WIDTH = VideoRenderer.VIDEO_WIDTH-XPOS;
	private static final int HEIGHT = 400;

	private final LineGraph graphBalance = new LineGraph();
	private final LineGraph graphAssets = new LineGraph();
	private final LineGraph graphTotal = new LineGraph();

	private final ActivityValue activity;

	private final long limitValue;

	public EDCreditsBalance(File f, ActivityValue a) throws IOException {
		if (f.exists()) {
			ArrayList<String> li = FileIO.getFileAsLines(f);
			for (String s : li) {
				String[] parts = s.split(",");
				DateStamp date = DateStamp.parse(parts[0]);
				long balance = Long.parseLong(parts[2]);
				long assets = Long.parseLong(parts[3]);
				long total = Long.parseLong(parts[4]);
				graphBalance.addPoint(date, balance);
				graphAssets.addPoint(date, assets);
				graphTotal.addPoint(date, total);
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
		int xctr = XPOS+WIDTH;
		int yctr = YPOS+HEIGHT;
		int yCr = yctr-this.getHeight(this.getBalance(date, graphBalance));
		int yAss = yctr-this.getHeight(this.getBalance(date, graphAssets));
		int yTot = yctr-this.getHeight(this.getBalance(date, graphTotal));

		g.setColor(Color.red);

		g.drawLine(xctr, yCr, XPOS, yCrPrev);

		g.setColor(new Color(0, 170, 0));
		g.drawLine(xctr, yAss, XPOS, yAssPrev);

		g.setColor(Color.BLUE);
		g.drawLine(xctr, yTot, XPOS, yTotPrev);
	}

	private int getHeight(long val) {
		return (int)(val*HEIGHT/limitValue);
	}

	private long getBalance(DateStamp date, LineGraph gr) {
		long val = (long)gr.getValueAt(date);
		if (!activity.isActiveAt(date)) {
			DateStamp last = activity.getLastActiveDateBefore(date);
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
