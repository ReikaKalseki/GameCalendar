package Reika.GameCalendar;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.Window;
import Reika.GameCalendar.Rendering.CalendarRenderer;

public class Main {
	/*
	DO NOT FORGET:
		KSP (NOT IN SHAREX FILES due to copy-upload)
		ELITE SESSIONS
		FACTORIO SESSIONS
		PORTAL MAPS & PETI PLAYTHROUGHS
		PSMEL
		SPACE ENGINEERS (CHECK STEAM AND DEDICATED SCREENSHOTS)
		2014 EVERYTHING
		CIV PLAYTHROUGHS (NOT SHAREX)*/

	//TODO:
	//checkboxes to show:
	//"bookmark" holidays like christmas and halloween
	//the "christmas break"
	//the "summer break" (uni bounds)
	//current date
	//events
	//each category
	//ongoing sections

	private static final UncaughtExceptionHandler defaultErrorHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println("Thread encountered an uncaught exception:");
			System.out.println(t.toString());
			e.printStackTrace();
		}

	};

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
		Window.create();
		System.out.println("Main method complete");
		System.exit(0);
	}
	/*
	public static Timeline getTimeline() {
		return timeline;
	}*/

	public static CalendarRenderer getGUI() {
		return gui;
	}

	public static Calendar getCalendar() {
		return calendar;
	}

}
