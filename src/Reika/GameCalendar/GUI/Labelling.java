package Reika.GameCalendar.GUI;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import Reika.GameCalendar.Rendering.CalendarRenderer;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

public class Labelling implements Runnable {

	public static final Labelling instance = new Labelling();

	private Pane field;
	private boolean loaded;

	private int width;
	private int height;

	private final HashMap<Month, Label> monthTexts = new HashMap();

	private Labelling() {

	}

	public void init(Pane p) {
		field = p;
		loaded = false;
	}

	public void setRenderParams(int w, int h) {
		if (w != width || h != height)
			loaded = false;
		width = w;
		height = h;
	}

	public void run() {
		if (!loaded) {
			field.getChildren().clear();
			monthTexts.clear();
			for (Month m : Month.values()) {
				Label l = new Label(m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				Font f = l.getFont();
				f = Font.font(f.getFamily(), f.getSize()*1.5);
				l.setFont(f);
				field.getChildren().add(l);
				monthTexts.put(m, l);
			}
			loaded = true;
		}
		double r = (width+height)/4D;
		for (Entry<Month, Label> e : monthTexts.entrySet()) {
			Month m = e.getKey();
			double angle = CalendarRenderer.getGuiAngle(360/12D*(m.ordinal()+0.5));
			double x = r*(1+Math.cos(angle));
			double y = r*(1-Math.sin(angle));
			Label l = e.getValue();
			l.layoutXProperty().set(x);
			l.layoutYProperty().set(y);
		}
	}

}
