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
split the p2 map dev into maps?

2020 and probably other years PeTI

ALL THE SCREENSHOTS

ALL THE "MEMORABLE" FLAGS
-2018 stellaris,
giant 2016 Civ V continents
2015 tour server (first half)
2015 elite
2016 AND 2018 sagA/colonia trips
2018 CivV vs china
1.6 SSP
1.1 survival world
space engineers intro and 2017 planet save
bobmods 0.11, 0.14
late 2015 factorio
shortlife server?
2018 factorio?
2015 PeTI
2015 portal map making
MORE

	 */

	//TODO:
	//checkboxes to show:
	//to merge all category arcs together so it just shows game->game ("show subsections")

	//ability to reload data

	//"export as movie" functionality!!!!!!!!!!!********************************************************

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
