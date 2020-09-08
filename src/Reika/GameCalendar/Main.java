package Reika.GameCalendar;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.Rendering.CalendarRenderer;
import Reika.GameCalendar.Rendering.RenderLoop;

public class Main {
	/*
	DO NOT FORGET:
		SPLIT THE CIV SAVES
		SPLIT THE FACTORIO SAVES (three 2020 sections [campaign, survival 1, survival2]
SPLIT THE SE PLAYS

ELITE 2020 ENGINEERING THE COBRA (may)
elite 2020 mat (mostly surface) hunting + overcharged multicannons: may 10 to 24
ELITE 2020 JUN 19 ANACONDA GRAVEYARD EVENT
elite 2020 july engineering the t10

ADD TUNGSTEN GEARBOXES TO THE APPROPRATE 2020 MODDING DESC (early may)

split the p2 maps?

2020 and probably other years PeTI

	 */

	//TODO:
	//ability to handle "concurrent" highlights


	//checkboxes to show:
	//ongoing sections

	//add another box to show a sample screenshot, linked to with a .png

	private static final UncaughtExceptionHandler defaultErrorHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println("Thread encountered an uncaught exception:");
			System.out.println(t.toString());
			e.printStackTrace();
		}

	};

	private static RenderLoop renderer;
	private static Timeline timeline;
	private static CalendarRenderer gui;

	private static final Calendar calendar = Calendar.getInstance();

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(defaultErrorHandler);
		timeline = new Timeline();
		File f = new File("Data");
		for (File in : f.listFiles()) {
			if (in.isDirectory()) {
				try {
					ActivityCategory.loadCategory(in, timeline);
				}
				catch (Exception e) {
					throw new RuntimeException("Could not load data folder '"+in.getName()+"'!", e);
				}
			}
		}
		timeline.prepare();
		gui = new CalendarRenderer(timeline);

		renderer = new RenderLoop();
		renderer.start();
		JFXWindow.create();

		//WavefrontObjDemoStandalone.main(args);
		//window.create();
		renderer.close();
		System.out.print("Exiting program");
		System.exit(0);
	}
	/*
	public static Timeline getTimeline() {
		return timeline;
	}*/

	public static CalendarRenderer getCalendarRenderer() {
		return gui;
	}

	public static Calendar getCalendar() {
		return calendar;
	}

	public static Timeline getTimeline() {
		return timeline;
	}

}
