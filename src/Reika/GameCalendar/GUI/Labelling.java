package Reika.GameCalendar.GUI;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import Reika.GameCalendar.Rendering.CalendarRenderer;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Labelling implements Runnable {

	public static final Labelling instance = new Labelling();

	private Pane field;
	private CalendarRenderer renderer;
	private boolean loaded;

	private int width;
	private int height;

	private final HashMap<Month, Label> monthTexts = new HashMap();
	private final HashMap<Integer, Label> yearTexts = new HashMap();

	private Labelling() {

	}

	public void init(Pane p) {
		field = p;
		loaded = false;
	}

	public void setRenderParams(int w, int h, CalendarRenderer r) {
		if (w != width || h != height)
			loaded = false;
		width = w;
		height = h;
		renderer = r;
	}

	public void run() {
		List<Integer> years = renderer.getYears();
		if (!loaded) {
			field.getChildren().clear();
			monthTexts.clear();
			for (Month m : Month.values()) {
				Label l = new Label(m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				Font f = l.getFont();
				f = Font.font(f.getFamily(), FontWeight.BOLD, f.getSize()*1.1);
				l.setFont(f);
				field.getChildren().add(l);
				monthTexts.put(m, l);
			}
			loaded = true;

			for (int year : years) {
				Label l = new Label(String.valueOf(year));
				Font f = l.getFont();
				f = Font.font(f.getFamily(), FontWeight.BOLD, f.getSize()*0.9);
				l.setFont(f);
				field.getChildren().add(l);
				yearTexts.put(year, l);
			}
		}
		double c = (width+height)/4D;
		for (Entry<Month, Label> e : monthTexts.entrySet()) {
			Month m = e.getKey();
			Label l = e.getValue();
			double a = 360/12D*(m.ordinal()+0.5);
			//System.out.println(m.name()+" > "+a+" @ '"+l.getText()+"'");
			double angle = CalendarRenderer.getGuiAngle(a);
			double r = renderer.getArcCenterlineRadiusAt(years.size(), a)-0.025;
			double x = c*(1+r*Math.cos(angle));
			double y = c*(1-r*Math.sin(angle));
			double w = l.getWidth();
			double h = l.getHeight();
			l.layoutXProperty().set(x-w/2);
			l.layoutYProperty().set(y-h/2);
			l.rotateProperty().set(a);
		}
		for (int i = 0; i < years.size(); i++) {
			int year = years.get(i);
			Label l = yearTexts.get(year);
			double r = renderer.getArcCenterlineRadiusAt(i, 0);
			double x = c;
			double y = c-r*c;
			double w = l.getWidth();
			double h = l.getHeight();
			l.layoutXProperty().set(x-w/2*0+5);
			l.layoutYProperty().set(y-h/2+1);
			l.setTextFill(Color.rgb(0, 0, 0, 1));
		}
	}

}
