package Reika.GameCalendar.GUI;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.Rendering.CalendarRenderer;
import Reika.GameCalendar.Rendering.RenderLoop;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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

	private Label tooltip;
	private final HashMap<Month, Label> monthTexts = new HashMap();
	private final HashMap<Integer, Label> yearTexts = new HashMap();

	private Label fps;

	public String tooltipString;

	private String descriptions = "";
	private int descriptionSize = 0;
	private boolean descriptionChanged;

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

			tooltip = new Label();
			Font f = tooltip.getFont();
			f = Font.font(f.getFamily(), FontWeight.BOLD, f.getSize()*1.25);
			tooltip.setFont(f);
			field.getChildren().add(tooltip);

			fps = new Label();
			f = fps.getFont();
			f = Font.font(f.getFamily(), FontWeight.NORMAL, f.getSize()*0.75);
			fps.setFont(f);
			field.getChildren().add(fps);
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

		if (RenderLoop.enableFPS) {
			fps.setVisible(true);
			fps.layoutXProperty().set(5);
			fps.layoutYProperty().set(5);
			fps.setText(String.valueOf(Main.getFPS()));
		}
		else {
			fps.setVisible(false);
		}

		if (descriptionChanged) {
			TextArea area = JFXWindow.getDescriptionPane();
			area.textProperty().set(descriptions);
			Font f = area.getFont();
			double sz = 12;
			if (descriptionSize > 8) {
				int over = descriptionSize-8;
				sz -= over*1;
			}
			sz = Math.max(sz, 11);
			area.setFont(new Font(f.getFamily(), sz));
			descriptionChanged = false;
		}

		tooltip.setBackground(new Background(new BackgroundFill(Color.rgb(1, 0, 0, 0.6), new CornerRadii(0), new Insets(0))));
		tooltip.toFront();
		tooltip.setText(tooltipString != null ? tooltipString : "");
		tooltip.setTextFill(Color.rgb(255, 255, 255, 1));
		tooltip.visibleProperty().set(tooltipString != null);
		tooltip.layoutXProperty().set(JFXWindow.getMouseHandler().getMouseX(true)+16);
		tooltip.layoutYProperty().set(JFXWindow.getMouseHandler().getMouseY(true)+16);
	}

	public void calculateDescriptions(ArrayList<CalendarEvent> li) {
		if (li == null) {
			descriptions = "";
			descriptionSize = 0;
		}
		else {
			ArrayList<String> desc = new ArrayList();
			for (int i = 0; i < li.size(); i++) {
				CalendarEvent ci = li.get(i);
				ci.generateDescriptionText(desc);
				if (i < li.size()-1 && !GuiElement.ARCMERGE.isChecked())
					desc.add("");
			}
			/*
			if (desc.size() >= 12) {
				Iterator<String> it = desc.iterator();
				while (it.hasNext()) {
					String sg = it.next();
					if (Strings.isNullOrEmpty(sg)) {
						it.remove();
					}
				}
			}*/
			descriptions = this.lineBreakStringList(desc);
			descriptionSize = desc.size();
		}
		descriptionChanged = true;
	}

	private String lineBreakStringList(List<String> li) {
		if (li.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < li.size(); i++) {
			sb.append(li.get(i));
			if (i < li.size()-1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public String getDescription() {
		return descriptions;
	}

}
