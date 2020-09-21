package Reika.GameCalendar.Rendering;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.CompoundElement;
import Reika.GameCalendar.Data.CondensedTimeline;
import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.Data.ImportantDates;
import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.CalendarItem;
import Reika.GameCalendar.GUI.DFXInputHandler;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.GUI.GuiHighlight;
import Reika.GameCalendar.GUI.GuiSection;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.Labelling;
import Reika.GameCalendar.Util.Colors;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePoint;
import Reika.GameCalendar.Util.DoublePolygon;
import Reika.GameCalendar.Util.GLFunctions.BlendMode;

import javafx.application.HostServices;
import javafx.application.Platform;

public class CalendarRenderer {

	private static final double INNER_RADIUS = 0.2;
	private static final double MAX_THICKNESS = 0.75;

	public static final Comparator<CalendarEvent> eventSorter = new Comparator<CalendarEvent>() {
		@Override
		public int compare(CalendarEvent o1, CalendarEvent o2) {
			int dc = o1.getDescriptiveDate().compareTo(o2.getDescriptiveDate());
			int cc = o1.category.compareTo(JFXWindow.getGUI().getSortingMode(), o2.category);
			return dc+cc*10000;
		}
	};

	private final Timeline data;

	private final ArrayList<GuiSection> sections = new ArrayList();
	private final ArrayList<GuiSection> sectionsCondensed = new ArrayList();

	private final HashMap<DateStamp, GuiHighlight> events = new HashMap();
	private final ArrayList<Integer> years;

	public DateStamp limit = null;

	public final double arcThickness;
	public final double arcThicknessHalfFraction = 0.35;

	private final Collection<CalendarItem> selectedObjects = new HashSet();
	private final Collection<CalendarItem> selectedObjectsCache = new ArrayList();

	public CalendarRenderer(Timeline t) {
		data = t;
		List<Section> li = t.getSections();
		for (int i = 0; i < li.size(); i++) {
			Section s = li.get(i);
			sections.add(new GuiSection(s, i, i == 0 ? null : sections.get(i-1)));
		}
		for (Highlight s : t.getEvents()) {
			GuiHighlight at = events.get(s.time);
			if (at == null) {
				at = new GuiHighlight(s);
				events.put(at.time, at);
			}
			else
				at.addEvent(s);
		}
		CondensedTimeline ct = new CondensedTimeline(t, 6);
		li = ct.getSections();
		for (int i = 0; i < li.size(); i++) {
			Section s = li.get(i);
			sectionsCondensed.add(new GuiSection(s, i, i == 0 ? null : sectionsCondensed.get(i-1)));
		}
		years = new ArrayList(t.getYears());
		Collections.sort(years);

		arcThickness = MAX_THICKNESS/years.size();
	}

	public List<Integer> getYears() {
		return Collections.unmodifiableList(years);
	}

	public synchronized void draw(int sw, int sh) {
		double t = System.currentTimeMillis();
		GL11.glLineWidth(2);
		GL11.glDepthMask(false);
		GL11.glColor4f(0, 0, 0, 1);
		for (int i = 0; i < years.size(); i++) {
			int year = years.get(i);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (double a = 0; a <= 360; a += 2) {
				double ang = this.getGuiAngle(a);
				double r = this.getArcCenterlineRadiusAt(i, a)-arcThickness*arcThicknessHalfFraction;
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				GL11.glVertex2d(x, y);
			}
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (double a = 0; a <= 360; a += 2) {
				double ang = this.getGuiAngle(a);
				double r = this.getArcCenterlineRadiusAt(i, a)+arcThickness*arcThicknessHalfFraction;
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				GL11.glVertex2d(x, y);
			}
			GL11.glEnd();
		}

		for (Month m : Month.values()) {
			GL11.glLineWidth(1);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2d(0, 0);
			double df = m.ordinal()/12D;
			double a = 360*df;
			double ang = this.getGuiAngle(a);
			double r = MAX_THICKNESS+INNER_RADIUS+arcThickness*(arcThicknessHalfFraction+df-1);
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
			GL11.glEnd();
		}

		GL11.glLineWidth(4);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(0, MAX_THICKNESS+INNER_RADIUS+arcThickness*arcThicknessHalfFraction);
		GL11.glEnd();
		if (limit != null || GuiElement.TODAY.isChecked()) {
			GL11.glLineWidth(3);
			GL11.glBegin(GL11.GL_LINES);
			DateStamp d = limit != null ? limit : DateStamp.launch;
			double lang = d.getAngle();
			double dayang = this.getGuiAngle(lang);
			double r1 = INNER_RADIUS;
			if (limit != null) {
				int i = years.indexOf(limit.year);
				r1 = INNER_RADIUS+i*arcThickness;
			}
			double r2 = r1+arcThickness;
			double ri = (r1+(r2-r1)*(lang/360D))-arcThickness*arcThicknessHalfFraction+0.002;
			double ro = ri+arcThickness*years.size()-arcThickness*arcThicknessHalfFraction+0.005;//ri+ty*tf*2;
			if (limit != null) {
				ro = ri+arcThickness*arcThicknessHalfFraction*2;
			}
			double dx1 = ri*Math.cos(dayang);
			double dy1 = ri*Math.sin(dayang);
			double dx2 = ro*Math.cos(dayang);
			double dy2 = ro*Math.sin(dayang);
			GL11.glVertex2d(dx1, dy1);
			GL11.glVertex2d(dx2, dy2);
			GL11.glEnd();
		}

		GL11.glLineWidth(2);
		double wf = 0.8;
		List<GuiSection> sec = this.getActiveSectionList();
		for (GuiSection s : sec) {
			s.polygon = null;
			s.skipRender = false;
			s.renderedEnd = s.section.getEnd();
		}
		for (GuiSection s : sec) {
			if (s.skipRender)
				continue;
			if (s.section.isEmpty())
				continue;
			if (limit != null && s.section.startTime.compareTo(limit) >= 0)
				continue;
			if (s.getActiveCategories().isEmpty())
				continue;
			this.drawSectionArc(s, wf, t, limit);
		}

		GL11.glLineWidth(2);

		for (GuiHighlight h : events.values()) {
			h.position = null;
		}

		if (GuiElement.HIGHLIGHTS.isChecked()) {
			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, -0.1);
			GL11.glPointSize(8);
			GL11.glColor4f(0, 0, 0, 1);
			GL11.glBegin(GL11.GL_POINTS);
			ArrayList<GuiHighlight> memorable = new ArrayList();
			for (GuiHighlight h : events.values()) {
				if (h.getActiveCategories().isEmpty())
					continue;
				if (limit != null && h.time.compareTo(limit) >= 0)
					continue;
				double a = h.time.getAngle();
				int i = years.indexOf(h.time.year);
				double r1 = INNER_RADIUS+i*arcThickness;
				double r2 = INNER_RADIUS+(i+1)*arcThickness;
				double r = r1+(r2-r1)*(a/360D);
				double ang = this.getGuiAngle(a);
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				h.position = new DoublePoint(x, y);
				GL11.glVertex2d(x, y);
				if (GuiElement.MEMORABLE.isChecked() && h.isMemorable(true)) {
					memorable.add(h);
				}
			}
			GL11.glEnd();

			if (!memorable.isEmpty()) {
				double d = 1/80D;
				GL11.glLineWidth(1);
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glBegin(GL11.GL_LINES);
				for (GuiHighlight h : memorable) {
					GL11.glVertex2d(h.position.x, h.position.y-d);
					GL11.glVertex2d(h.position.x, h.position.y+d);
					GL11.glVertex2d(h.position.x-d, h.position.y);
					GL11.glVertex2d(h.position.x+d, h.position.y);
				}
				GL11.glEnd();
				GL11.glLineWidth(2);
			}

			for (CalendarItem ci : selectedObjects) {
				if (ci instanceof GuiHighlight) {
					GuiHighlight gh = (GuiHighlight)ci;
					double d = 1/50D;
					GL11.glBegin(GL11.GL_LINE_LOOP);
					for (double a = 0; a < 360; a += 60) {
						double dx = d*Math.cos(Math.toRadians(a+90));
						double dy = d*Math.sin(Math.toRadians(a+90));
						GL11.glVertex2d(gh.position.x+dx, gh.position.y+dy);
					}
					GL11.glEnd();
				}
			}

			GL11.glPopMatrix();
		}

		if (GuiElement.XMAS.isChecked()) {
			this.drawSplitTimeWedge(t, DateStamp.xmasStart, DateStamp.xmasFull, DateStamp.xmasEnd, 0xff0000, 0x007700);
		}

		if (GuiElement.READING.isChecked()) {
			this.drawTimeWedge(t, DateStamp.readingStart, DateStamp.readingEnd, 0xc0c0c0, 0x00ffff);
		}

		if (GuiElement.SUMMER.isChecked()) {
			this.drawSplitTimeWedge(t, DateStamp.summerStart, DateStamp.summerFull, DateStamp.summerEnd, 0xffff00, 0x00ff00);
		}

		if (GuiElement.HOLIDAYS.isChecked()) {
			for (ImportantDates d : ImportantDates.dates) {
				GL11.glColor4f(0, 0, 0, 1);
				GL11.glBegin(GL11.GL_LINE_STRIP);
				GL11.glVertex2d(0, 0);
				for (int i = 0; i < years.size(); i++) {
					int year = years.get(i);
					DateStamp date = d.getDate(year);
					double a = date.getAngle();
					double ang = this.getGuiAngle(a);
					double r = this.getArcCenterlineRadiusAt(i, a);
					double x = r*Math.cos(ang);
					double y = r*Math.sin(ang);
					GL11.glVertex2d(x, y);
				}
				GL11.glEnd();
			}
		}

		this.updateLabels(sw, sh);
	}

	private void updateLabels(int sw, int sh) {
		if (VideoRenderer.instance.isRendering()) {
			Labelling.instance.tooltipString = null;
		}
		else {
			DFXInputHandler dfx = JFXWindow.getGUI().getMouseHandler();
			double mx = dfx.getMouseX(false);
			double my = dfx.getMouseY(false);
			double r0 = Math.sqrt(mx*mx+my*my);
			if (r0 >= INNER_RADIUS-arcThickness*arcThicknessHalfFraction && r0 <= INNER_RADIUS+arcThickness*(years.size()+1)) {
				double ang = (Math.toDegrees(Math.atan2(mx, my))+360)%360;
				double r = r0-ang*arcThickness/360;
				int idx = (int)Math.floor(ang*12/360);
				Month m = Month.of(1+idx);
				double frac = (ang-idx*360/12)/(360/12);
				int yearidx = (int)Math.round((r-INNER_RADIUS)/(arcThickness));//(int)Math.round((r-INNER_RADIUS)*0.5*years.size()/(MAX_THICKNESS-INNER_RADIUS));
				if (r0 >= INNER_RADIUS-arcThickness*arcThicknessHalfFraction && yearidx >= 0 && yearidx < years.size()) {
					int year = years.get(yearidx);
					int day = (int)Math.ceil(frac*m.length(DateStamp.isLeapYear(year)));
					Labelling.instance.tooltipString = m.getDisplayName(TextStyle.SHORT, Locale.getDefault())+" "+day+", "+year;
				}
				else {
					Labelling.instance.tooltipString = null;
				}
			}
			else {
				Labelling.instance.tooltipString = null;
			}
		}

		Labelling.instance.setRenderParams(sw, sh, this);
		Platform.runLater(Labelling.instance);
	}

	private void drawSectionArc(GuiSection s, double wf, double t, DateStamp limit) {
		double a1 = s.angleStart;
		double a2 = s.angleEnd;
		DateStamp end = s.section.getEnd();
		GuiSection g2 = s.getNext();
		GuiSection g2b = s;
		while (g2 != null && this.shouldMerge(s, g2)) {
			g2.skipRender = true;
			a2 = g2.angleEnd;
			end = g2.section.getEnd();
			g2b = g2;
			g2 = g2.getNext();
		}
		s.renderedEnd = g2b.section.getEnd();
		if (limit != null && end.compareTo(limit) > 0) {
			end = limit;
			s.renderedEnd = end;
			a2 = end.getAngle();
		}
		int i1 = years.indexOf(s.section.startTime.year);
		int i2 = years.indexOf(end.year);
		double r1a = INNER_RADIUS+i1*arcThickness;
		double r1b = INNER_RADIUS+(i1+1)*arcThickness;
		double r2a = INNER_RADIUS+i2*arcThickness;
		double r2b = INNER_RADIUS+(i2+1)*arcThickness;
		while (a1 > a2) { //across a new year
			a2 += 360;
			r2b -= arcThickness;
		}
		int years = (int)Math.ceil(s.section.startTime.countDaysAfter(s.renderedEnd)/365D);
		if (years > 1)
			a2 += 360*(years-1);
		ArrayList<DoublePoint> pointsCenterline = new ArrayList();
		ArrayList<DoublePoint> pointsInner = new ArrayList();
		ArrayList<DoublePoint> pointsOuter = new ArrayList();
		LinkedList<DoublePoint> pointsInnerWide = new LinkedList();
		LinkedList<DoublePoint> pointsOuterWide = new LinkedList();
		int colorstep = 2;//Math.max(4, 6-i2/2);
		for (double a = a1; a < a2; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r1 = r1a;
			double r2 = r1b;

			double r0 = r1+(r2-r1)*(a/360D);

			double ra = r0-arcThickness*arcThicknessHalfFraction*wf;
			double rb = r0+arcThickness*arcThicknessHalfFraction*wf;

			double ra2 = ra-arcThickness*arcThicknessHalfFraction*wf*0.25;
			double rb2 = rb+arcThickness*arcThicknessHalfFraction*wf*0.25;

			double xa = ra*Math.cos(ang);
			double ya = ra*Math.sin(ang);
			double xb = rb*Math.cos(ang);
			double yb = rb*Math.sin(ang);

			double xa2 = ra2*Math.cos(ang);
			double ya2 = ra2*Math.sin(ang);
			double xb2 = rb2*Math.cos(ang);
			double yb2 = rb2*Math.sin(ang);

			double x0 = r0*Math.cos(ang);
			double y0 = r0*Math.sin(ang);

			pointsInner.add(new DoublePoint(xa, ya));
			pointsOuter.add(new DoublePoint(xb, yb));
			pointsInnerWide.addLast(new DoublePoint(xa2, ya2));
			pointsOuterWide.addFirst(new DoublePoint(xb2, yb2));

			pointsCenterline.add(new DoublePoint(x0, y0));
		}
		if (pointsInner.isEmpty()) {
			return;
		}
		s.polygon = new DoublePolygon();
		ArrayList<DoublePoint> points = new ArrayList();
		ArrayList<DoublePoint> pointsWide = new ArrayList();
		for (int i = 0; i < pointsInner.size(); i++) {
			points.add(pointsInner.get(i));
			points.add(pointsOuter.get(i));
		}
		pointsWide.addAll(pointsInnerWide);
		pointsWide.addAll(pointsOuterWide);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		int clr = 0xffffff;
		int i = 0;
		for (DoublePoint p : points) {
			clr = this.getSectionColorAtIndex(s, i/colorstep);
			if (GuiElement.MEMORABLE.isChecked() && s.isMemorable(true)) {
				//float f = (float)(0.75+0.125*Math.sin(i*0.55-t*0.027)+0.125*Math.cos(i*0.37-t*0.017));
				//clr = Colors.mixColors(clr, 0xffffff, f);
			}
			float r = Colors.HextoColorMultiplier(clr, 0);
			float g = Colors.HextoColorMultiplier(clr, 1);
			float b = Colors.HextoColorMultiplier(clr, 2);
			GL11.glColor4f(r, g, b, 1);
			GL11.glVertex2d(p.x, p.y);
			i++;
		}
		GL11.glEnd();
		points.clear();
		points.addAll(pointsInner);
		Collections.reverse(pointsOuter);
		points.addAll(pointsOuter);
		boolean sel = selectedObjects.contains(s);
		if (sel) {
			GL11.glColor4f(0, 0, 0, 1);
			GL11.glBegin(GL11.GL_LINE_LOOP);
		}
		for (int idx = 0; idx < points.size(); idx++) {
			DoublePoint p = points.get(idx);
			DoublePoint p2 = pointsWide.get(idx);
			if (sel)
				GL11.glVertex2d(p.x, p.y);
			s.polygon.addPoint(p2.x, p2.y);
		}
		if (sel)
			GL11.glEnd();
		if (GuiElement.MEMORABLE.isChecked() && s.isMemorable(true)) {
			GL11.glLineWidth(12);
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			short bits = 0x7070;
			int d = (int)((t/60D)%16);
			short pattern = bits;//(short)((bits << d) | (bits >> (16 - d)));
			GL11.glLineStipple(1, pattern);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int idx = 0; idx < pointsCenterline.size(); idx++) {
				DoublePoint p = pointsCenterline.get(idx);
				clr = this.getSectionColorAtIndex(s, idx);
				clr = Colors.mixColors(clr, 0xffffff, 0.6F);
				float r = Colors.HextoColorMultiplier(clr, 0);
				float g = Colors.HextoColorMultiplier(clr, 1);
				float b = Colors.HextoColorMultiplier(clr, 2);
				GL11.glColor4f(r, g, b, 1);
				GL11.glVertex2d(p.x, p.y);
			}
			/*
			GL11.glBegin(GL11.GL_LINE_LOOP);
			for (DoublePoint p : pointsWide) {
				GL11.glVertex2d(p.x, p.y);
			}*/
			GL11.glEnd();
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GL11.glLineWidth(2);
		}
	}

	private boolean shouldMerge(GuiSection g1, GuiSection g2) {
		/*
		if (GuiElement.ARCMERGE.isChecked()) {
			GuiSection g0 = g1;
			GuiSection g2b = g2.getNext();
			while (g2b != null && g2.getActiveCategories().isEmpty()) {
				g1 = g2;
				g2 = g2b;
				g2b = g2.getNext();
			}
			if (g1.getActiveCategories().equals(g2.getActiveCategories()) && g1.renderedEnd.countDaysAfter(g2.section.startTime) <= 9)
				return true;
		}*/
		return g2.getActiveSpans().equals(g1.getActiveSpans());
	}

	private void drawTimeWedge(double t, DateStamp d1, DateStamp d2, int c1, int c2) {
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		float cm = (float)(0.5+0.5*Math.sin(t/400D));
		int mix = Colors.mixColors(c1, c2, cm);
		float red = Colors.getRed(mix)/255F;
		float green = Colors.getGreen(mix)/255F;
		float blue = Colors.getBlue(mix)/255F;
		GL11.glColor4f(red, green, blue, 1);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(0, 0);
		double a0 = d1.getAngle();
		double a1 = d2.getAngle();
		if (a1 < a0)
			a1 += 360;
		double divAng = this.getGuiAngle(a1);
		double divR = this.getArcCenterlineRadiusAt(years.size()-1, a1)+arcThickness*(arcThicknessHalfFraction+0.125);
		double divX = divR*Math.cos(divAng);
		double divY = divR*Math.sin(divAng);
		for (double a = a0; a < a1; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r = this.getArcCenterlineRadiusAt(years.size()-1, a)+arcThickness*(arcThicknessHalfFraction+0.125);//MAX_THICKNESS;
			if (a >= 361)
				r -= arcThickness;
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();
		GL11.glColor4f(red, green, blue, 0.5F);
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glLineStipple(2, (short)0x7777);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(divX, divY);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
		double f = 0.1875+0.0625*Math.sin(t/500D);
		GL11.glColor4f(red, green, blue, (float)f);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2d(0, 0);
		for (double a = a0; a <= a1; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r = this.getArcCenterlineRadiusAt(years.size()-1, a)+arcThickness*(arcThicknessHalfFraction+0.125);//MAX_THICKNESS;
			if (a >= 361)
				r -= arcThickness;
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawSplitTimeWedge(double t, DateStamp d1, DateStamp d2, DateStamp d3, int c1, int c2) {
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		float cm = (float)(0.5+0.5*Math.sin(t/400D));
		int mix = Colors.mixColors(c1, c2, cm);
		float red = Colors.getRed(mix)/255F;
		float green = Colors.getGreen(mix)/255F;
		float blue = Colors.getBlue(mix)/255F;
		GL11.glColor4f(red, green, blue, 1);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(0, 0);
		double a0 = d1.getAngle();
		double a1 = d2.getAngle();
		double a2 = d3.getAngle();
		if (a1 < a0)
			a1 += 360;
		if (a2 < a1)
			a2 += 360;
		double divAng = this.getGuiAngle(a1);
		double divR = this.getArcCenterlineRadiusAt(years.size()-1, a1)+arcThickness*(arcThicknessHalfFraction+0.125);
		double divX = divR*Math.cos(divAng);
		double divY = divR*Math.sin(divAng);
		for (double a = a0; a < a2; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r = this.getArcCenterlineRadiusAt(years.size()-1, a)+arcThickness*(arcThicknessHalfFraction+0.125);//MAX_THICKNESS;
			if (a >= 361)
				r -= arcThickness;
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();
		GL11.glColor4f(red, green, blue, 0.5F);
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glLineStipple(2, (short)0x7777);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(divX, divY);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
		double f = 0.1875+0.0625*Math.sin(t/500D);
		GL11.glColor4f(red, green, blue, (float)f*0.4F);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2d(0, 0);
		for (double a = a0; a <= a1; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r = this.getArcCenterlineRadiusAt(years.size()-1, a)+arcThickness*(arcThicknessHalfFraction+0.125);//MAX_THICKNESS;
			if (a >= 361)
				r -= arcThickness;
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();

		GL11.glColor4f(red, green, blue, (float)f);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2d(0, 0);
		for (double a = a1; a <= a2; a += 0.5) {
			double ang = this.getGuiAngle(a);
			double r = this.getArcCenterlineRadiusAt(years.size()-1, a)+arcThickness*(arcThicknessHalfFraction+0.125);//MAX_THICKNESS;
			if (a >= 361)
				r -= arcThickness;
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
	}

	public double getArcCenterlineRadiusAt(int i, double a) {
		double r1 = INNER_RADIUS+i*arcThickness;
		double r2 = r1+arcThickness;
		return (r1+(r2-r1)*(a/360D));
	}

	private int getSectionColorAtIndex(GuiSection s, int i) {
		List<TimeSpan> li = s.getActiveSpans();
		if (li.isEmpty())
			return 0x000000;
		/*
		double t = (System.currentTimeMillis()/200D+i/24D);
		int n1 = (int)(t%li.size());
		int n2 = (n1+1)%li.size();
		double f = t-(long)t;
		TimeSpan sp1 = li.get(n1);
		TimeSpan sp2 = li.get(n2);
		int c1 = sp1.getColor();
		int c2 = sp2.getColor();
		return Colors.mixColors(c1, c2, 1-(float)f);
		 */
		int n = i%li.size();
		TimeSpan sp = li.get(n);
		return sp.getColor();
	}

	public static double getGuiAngle(double a) {
		return Math.toRadians(-a+90);
	}

	private List<GuiSection> getActiveSectionList() {
		return GuiElement.ARCMERGE.isChecked() ? sectionsCondensed : sections;
	}

	public synchronized void handleMouse(double x, double y) {
		if (VideoRenderer.instance.isRendering())
			return;
		selectedObjects.clear();
		if (GuiElement.HIGHLIGHTS.isChecked()) {
			double d = 1/50D;
			for (GuiHighlight h : events.values()) {
				if (h.position != null)	{
					if (Math.abs(x-h.position.x) < d && Math.abs(y-h.position.y) < d) {
						//System.out.println(mx+","+my+" > "+s.section);
						selectedObjects.add(h);
						if (GuiElement.SECTIONSWITHHIGHLIGHT.isChecked()) {
							GuiSection s = this.getSectionAt(h.time);
							if (s != null && !s.getActiveCategories().isEmpty() && !selectedObjects.contains(s)) {
								selectedObjects.add(s);
							}
						}
						//break;
					}
				}
			}
		}
		if (selectedObjects.isEmpty()) {
			for (GuiSection s : this.getActiveSectionList()) {
				if (s.polygon != null && s.polygon.npoints > 0)	{
					if (s.polygon.contains(x, y)) {
						//System.out.println(mx+","+my+" > "+s.section);
						selectedObjects.add(s);
						if (GuiElement.HIGHLIGHTS.isChecked() && GuiElement.HIGHLIGHTSINSECTION.isChecked()) {
							for (GuiHighlight h : events.values()) {
								if (h.time.isBetween(s.section.startTime, s.renderedEnd) && !h.getActiveCategories().isEmpty()) {
									selectedObjects.add(h);
								}
							}
						}
						break;
					}
				}
			}
		}

		this.calculateDescriptions();
	}

	public void calculateDescriptions() {
		ArrayList<CalendarEvent> li = new ArrayList();
		if (selectedObjects.isEmpty()) {
			JFXWindow.getGUI().setScreenshots(null);
		}
		else {
			for (CalendarItem ci : selectedObjects) {
				for (CalendarEvent ce : ci.getItems(true)) {
					if (ce instanceof CompoundElement) {
						li.addAll(((CompoundElement)ce).getElements());
					}
					else
						li.add(ce);
				}
			}
			Collections.sort(li, eventSorter);
			JFXWindow.getGUI().setScreenshots(li);
		}
		Labelling.instance.calculateDescriptions(li);
	}

	public void openSelectedFiles(HostServices host) {
		for (CalendarItem ci : selectedObjects) {
			for (CalendarEvent ce : ci.getItems(true)) {
				ce.openFile(host);
			}
		}
	}

	public synchronized void clearSelection() {
		selectedObjects.clear();
		Labelling.instance.calculateDescriptions(null);
	}

	public synchronized void preserveSelection() {
		selectedObjectsCache.clear();
		selectedObjectsCache.addAll(selectedObjects);
	}

	public synchronized void restoreSelection() {
		selectedObjects.clear();
		selectedObjects.addAll(selectedObjectsCache);
		selectedObjectsCache.clear();
		this.calculateDescriptions();
	}

	public GuiSection getSectionAt(DateStamp date) {
		for (GuiSection s : this.getActiveSectionList()) {
			if (date.isBetween(s.section.startTime, s.section.getEnd())) {
				return s;
			}
		}
		return null;
	}

	public ArrayList<GuiHighlight> getHighlightsInSection(GuiSection s) {
		ArrayList<GuiHighlight> ret = new ArrayList();
		for (GuiHighlight h : events.values()) {
			if (h.time.isBetween(s.section.startTime, s.renderedEnd)) {
				ret.add(h);
			}
		}
		return ret;
	}

	public GuiHighlight getHighlightAtDate(DateStamp date) {
		return events.get(date);
	}
}
