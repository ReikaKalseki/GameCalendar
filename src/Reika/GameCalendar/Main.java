package Reika.GameCalendar;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.Rendering.CalendarRenderer;
import Reika.GameCalendar.Rendering.RenderLoop;

import javafx.application.Platform;

public class Main {
	/*
	DO NOT FORGET:
split the p2 map dev into maps?

2020 and probably other years PeTI

///figure out what minecraft you were doing in late april 2012
///and from aug 22 to sep 10
 // AND SPLIT OUT THE FIRST ESCAPECRAFT ITERATION
  // and the bc world looks like it starts at the same time as ethanoil - that cannot be right->the former is likely too late
   //big gap at jun 17 to jul 15 2013!
	 * and sep 6 to 19 too
	 * and nov 20 to 29
	 * sep 23 to oct 4 2014
	 * may 4 to 13 2015

ALL THE SCREENSHOTS

early-on rainforest server map reset?

ALL THE "MEMORABLE" FLAGS
bobmods 0.11
shortlife server?
2015 PeTI
MORE

	 */

	//TODO:
	//checkboxes to show:
	//to merge all category arcs together so it just shows game->game ("show subsections")

	//fix: being able to click or change the calendar renderer during video prep

	//"export as movie" functionality!!!!!!!!!!!********************************************************

	private static final UncaughtExceptionHandler defaultErrorHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println("Thread encountered an uncaught exception:");
			System.out.println(t.toString());
			e.printStackTrace();
			Platform.exit();
		}

	};

	private static RenderLoop renderer;
	private static Timeline timeline;
	private static CalendarRenderer gui;

	private static final Calendar calendar = Calendar.getInstance();

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(defaultErrorHandler);

		load();

		renderer = new RenderLoop();
		renderer.start();
		JFXWindow.create();

		//WavefrontObjDemoStandalone.main(args);
		//window.create();
		renderer.close();
		System.out.print("Exiting program");
		System.exit(0);
	}

	public static void load() {
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
	}

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
