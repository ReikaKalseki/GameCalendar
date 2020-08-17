package Reika.GameCalendar.GUI;

import java.awt.Color;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.opengl.GL11;

import Reika.GameCalendar.Data.Timeline;

public class GuiSystem {

	private static final double INNER_RADIUS = 0.2;
	private static final double MAX_THICKNESS = 0.6;

	private final Timeline data;
	private final ArrayList<GuiSection> sections = new ArrayList();
	private final ArrayList<Integer> years;

	public GuiSystem(Timeline t) {
		data = t;
		for (Section s : t.getSections()) {
			sections.add(new GuiSection(s));
		}
		years = new ArrayList(t.getYears());
		Collections.sort(years);
	}

	public void draw() {
		double tf = 0.35;
		double ty = MAX_THICKNESS/years.size();
		for (int i = 0; i < years.size(); i++) {
			double r1 = INNER_RADIUS+i*ty;
			double r2 = r1+ty;
			int year = years.get(i);
			GL11.glColor4f(0, 0, 0, 1);
			GL11.glLineWidth(2);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (double a = 0; a <= 360; a += 2) {
				double ang = this.getGuiAngle(a);
				double r = (r1+(r2-r1)*(a/360D))-ty*tf;
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				GL11.glVertex2d(x, y);
			}
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (double a = 0; a <= 360; a += 2) {
				double ang = this.getGuiAngle(a);
				double r = (r1+(r2-r1)*(a/360D))+ty*tf;
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
			double r = MAX_THICKNESS+INNER_RADIUS+ty*(tf+df-1);
			double x = r*Math.cos(ang);
			double y = r*Math.sin(ang);
			GL11.glVertex2d(x, y);
			GL11.glEnd();
		}

		GL11.glLineWidth(4);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(0, MAX_THICKNESS+INNER_RADIUS+ty*tf);
		GL11.glEnd();

		GL11.glLineWidth(4);
		for (GuiSection s : sections) {
			int clr = Color.HSBtoRGB(s.hashCode()/(float)Integer.MAX_VALUE, 1, 1);
			int bu = (clr & 0xFF);
			int gn = (clr >> 8 & 0xFF);
			int rd = (clr >> 16 & 0xFF);
			GL11.glColor4f(rd/255F, gn/255F, bu/255F, 1);
			GL11.glBegin(GL11.GL_LINE_STRIP);

			double a1 = s.section.startTime.getAngle();
			double a2 = s.section.getEnd().getAngle();
			int i1 = years.indexOf(s.section.startTime.year);
			int i2 = years.indexOf(s.section.getEnd().year);
			double r1 = INNER_RADIUS+i1*ty;
			double r2 = INNER_RADIUS+(i2+1)*ty;
			for (double a = a1; a <= a2; a += 2) {
				double ang = this.getGuiAngle(a);
				double r = r1+(r2-r1)*(a/360D);
				double x = r*Math.cos(ang);
				double y = r*Math.sin(ang);
				GL11.glVertex2d(x, y);
			}

			GL11.glEnd();
		}
	}

	private double getGuiAngle(double a) {
		return Math.toRadians(-a+90);
	}

}
