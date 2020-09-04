package Reika.GameCalendar.Rendering;

import java.awt.Polygon;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import Reika.GameCalendar.Data.Highlight;
import Reika.GameCalendar.Data.Section;
import Reika.GameCalendar.Data.TimeSpan;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.GuiSection;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.Labelling;
import Reika.GameCalendar.Util.Colors;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.DoublePoint;
import Reika.GameCalendar.Util.GLFunctions.BlendMode;

import javafx.application.Platform;

public class CalendarRenderer {

	private static final double INNER_RADIUS = 0.2;
	private static final double MAX_THICKNESS = 0.75;

	private final Timeline data;
	private final ArrayList<GuiSection> sections = new ArrayList();
	private final ArrayList<Highlight> events = new ArrayList();
	private final ArrayList<Integer> years;

	private final double arcThickness;
	private final double arcThicknessHalfFraction = 0.35;

	private GuiSection selectedSection = null;

	public CalendarRenderer(Timeline t) {
		data = t;
		for (Section s : t.getSections()) {
			sections.add(new GuiSection(s));
		}
		events.addAll(t.getEvents());
		years = new ArrayList(t.getYears());
		Collections.sort(years);

		arcThickness = MAX_THICKNESS/years.size();
	}

	public List<Integer> getYears() {
		return Collections.unmodifiableList(years);
	}

	public void draw(int sw, int sh) {
		double t = System.currentTimeMillis();
		GL11.glLineWidth(2);
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
		if (JFXWindow.getGUI().getCheckbox("currentDate")) {
			GL11.glLineWidth(3);
			GL11.glBegin(GL11.GL_LINES);
			double lang = DateStamp.launch.getAngle();
			double dayang = this.getGuiAngle(lang);
			double r1 = INNER_RADIUS;
			double r2 = r1+arcThickness;
			double ri = (r1+(r2-r1)*(lang/360D))-arcThickness*arcThicknessHalfFraction+0.002;
			double ro = ri+arcThickness*years.size()-arcThickness*arcThicknessHalfFraction+0.005;//ri+ty*tf*2;
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
		for (GuiSection s : sections) {
			if (s.section.isEmpty())
				continue;
			if (!this.shouldRenderSection(s))
				continue;
			double a1 = s.section.startTime.getAngle();
			double a2 = s.section.getEnd().getAngle();
			int i1 = years.indexOf(s.section.startTime.year);
			int i2 = years.indexOf(s.section.getEnd().year);
			double r1a = INNER_RADIUS+i1*arcThickness;
			double r1b = INNER_RADIUS+(i1+1)*arcThickness;
			double r2a = INNER_RADIUS+i2*arcThickness;
			double r2b = INNER_RADIUS+(i2+1)*arcThickness;
			if (a1 > a2) { //across the new year
				a2 += 360;
				r2b -= arcThickness;
			}
			ArrayList<DoublePoint> pointsInner = new ArrayList();
			ArrayList<DoublePoint> pointsOuter = new ArrayList();
			int colorstep = 2;//Math.max(4, 6-i2/2);
			for (double a = a1; a < a2; a += 0.5) {
				double ang = this.getGuiAngle(a);
				double r1 = r1a;
				double r2 = r2b;
				double ra = r1+(r2-r1)*(a/360D)-arcThickness*arcThicknessHalfFraction*wf;
				double rb = r1+(r2-r1)*(a/360D)+arcThickness*arcThicknessHalfFraction*wf;
				double xa = ra*Math.cos(ang);
				double ya = ra*Math.sin(ang);
				double xb = rb*Math.cos(ang);
				double yb = rb*Math.sin(ang);
				pointsInner.add(new DoublePoint(xa, ya));
				pointsOuter.add(new DoublePoint(xb, yb));
			}
			if (pointsInner.isEmpty()) {
				continue;
			}
			s.polygon = new Polygon();
			ArrayList<DoublePoint> points = new ArrayList();
			for (int i = 0; i < pointsInner.size(); i++) {
				points.add(pointsInner.get(i));
				points.add(pointsOuter.get(i));
			}
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			int clr = 0xffffff;
			int i = 0;
			for (DoublePoint p : points) {
				clr = this.getSectionColorAtIndex(s.section, i/colorstep);
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
			if (s == selectedSection) {
				GL11.glColor4f(0, 0, 0, 1);
				GL11.glBegin(GL11.GL_LINE_LOOP);
			}
			for (DoublePoint p : points) {
				if (s == selectedSection)
					GL11.glVertex2d(p.x, p.y);
				int lx = (int)(p.x*sw/2D+sw/2D);
				int ly = (int)(p.y*sh/2D+sh/2D);
				s.polygon.addPoint(lx-GLFWWindow.BORDER_X, ly-GLFWWindow.BORDER_Y*3/4);
			}
			if (s == selectedSection)
				GL11.glEnd();
		}

		if (JFXWindow.getGUI().getCheckbox("highlights")) {
			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, -0.1);
			GL11.glPointSize(8);
			GL11.glColor4f(0, 0, 0, 1);
			GL11.glBegin(GL11.GL_POINTS);
			for (Highlight h : events) {
				if (!JFXWindow.getGUI().isListEntrySelected("catList", h.category.name))
					continue;
				double a = h.time.getAngle();
				int i = years.indexOf(h.time.year);
				double r1 = INNER_RADIUS+i*arcThickness;
				double r2 = INNER_RADIUS+(i+1)*arcThickness;
				double r = r1+(r2-r1)*(a/360D);
				double ang = this.getGuiAngle(a);
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				GL11.glVertex2d(x, y);
			}
			GL11.glEnd();
			GL11.glPopMatrix();
		}

		if (JFXWindow.getGUI().getCheckbox("xmasBreak")) {
			this.drawSplitTimeWedge(t, DateStamp.xmasStart, DateStamp.xmasFull, DateStamp.xmasEnd, 0xff0000, 0x007700);
		}

		if (JFXWindow.getGUI().getCheckbox("summerBreak")) {
			this.drawSplitTimeWedge(t, DateStamp.summerStart, DateStamp.summerFull, DateStamp.summerEnd, 0xffff00, 0x00ffff);
		}

		Labelling.instance.setRenderParams(sw, sh, this);
		Platform.runLater(Labelling.instance);

		/*
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		String text = "testString";
		ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
		GL32.glEnableClientState(GL32.GL_VERTEX_ARRAY);
		GL32.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);
		GL11.glPushMatrix();
		int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
		GL32.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
		GL11.glPopMatrix();
		GL32.glDisableClientState(GL32.GL_VERTEX_ARRAY);*/
	}

	private void drawSplitTimeWedge(double t, DateStamp d1, DateStamp d2, DateStamp d3, int c1, int c2) {
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		float cm = (float)(0.5+0.5*Math.sin(t/400D));
		GL11.glColor4f(1-cm, cm*0.5F, 0, 1);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(0, 0);
		double a0 = d1.getAngle();
		double a1 = d2.getAngle();
		double a2 = d3.getAngle()+360;
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
		GL11.glColor4f(1-cm, cm*0.5F, 0, 0.5F);
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glLineStipple(2, (short)0x7777);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(divX, divY);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
		double f = 0.1875+0.0625*Math.sin(t/500D);
		GL11.glColor4f(1-cm, cm*0.5F, 0, (float)f*0.4F);
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

		GL11.glColor4f(1-cm, cm*0.5F, 0, (float)f);
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

	private boolean shouldRenderSection(GuiSection s) {
		for (TimeSpan ts : s.section.getActiveSpans()) {
			if (JFXWindow.getGUI().isListEntrySelected("catList", ts.category.name)) {
				return true;
			}
		}
		return false;
	}

	private int getSectionColorAtIndex(Section s, int i) {
		List<TimeSpan> li = new ArrayList(s.getActiveSpans());
		Iterator<TimeSpan> it = li.iterator();
		while (it.hasNext()) {
			TimeSpan ts = it.next();
			if (!JFXWindow.getGUI().isListEntrySelected("catList", ts.category.name))
				it.remove();
		}
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

	public void handleMouse(int w, int h) {
		//int mx = Mouse.getX();
		//int my = Mouse.getY();
		/*
		ArrayList<DoublePoint> points = new ArrayList();
		points.add(new DoublePoint(-0.25, -0.25));
		points.add(new DoublePoint(-0.35, 0.15));
		points.add(new DoublePoint(0.05, 0.25));
		points.add(new DoublePoint(-0.1, -0.3));
		Polygon poly = new Polygon();
		Collections.reverse(points);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glColor4f(0, 0, 1, 1);
		for (DoublePoint p : points) {
			GL11.glVertex2d(p.x, p.y);
			int lx = (int)((p.x*size.width+size.width)/2D);
			int ly = (int)((p.y*size.height+size.height)/2D);
			poly.addPoint(lx, ly);
		}
		GL11.glEnd();
		int d = 2;
		GL11.glPointSize(1F);
		GL11.glBegin(GL11.GL_POINTS);
		for (int i = 0; i < size.width; i += d) {
			for (int k = 0; k < size.height; k += d) {
				double px = ((i/(double)size.width)-0.5)*2;
				double py = ((k/(double)size.height)-0.5)*2;
				if (poly.contains(i, k)) {
					GL11.glColor4f(0, 1, 0, 1);
				}
				else {
					GL11.glColor4f(1, 0, 0, 1);
				}
				GL11.glVertex2d(px, py);
			}
		}
		GL11.glEnd();
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			System.out.println(mx+","+my);
		if (poly.contains(mx, my)) {
			GL11.glColor4f(0, 0, 0, 1);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2d(0, 0);
			GL11.glVertex2d(1, 1);
			GL11.glEnd();
		}
		 */
		/*
		if (Mouse.isButtonDown(0)) {
			selectedSection = null;
			//System.out.println(mx+","+my);
		}
		for (GuiSection s : sections) {
			if (s.polygon != null && s.polygon.npoints > 0)	{
				if (s.polygon.contains(mx, my)) {
					//System.out.println(mx+","+my+" > "+s.section);
					if (Mouse.isButtonDown(0)) {
						selectedSection = s;
					}
				}
			}
		}*/
	}
}
