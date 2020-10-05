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

	//TODO:
	//improve FB->video performance?

	//fmpeg admin problem

	//"Select all in year" is selecting too much, likely due to 'transitive leakage', i.e a section extending a little out of the year,
	//'contaminating' all within that section, and then selecting every section that contains THOSE

	//related: select all of category selects the entire section with any subelements of a given category, including all ones NOT of that cat

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
	private static String filepath;

	private static final Calendar calendar = Calendar.getInstance();

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(defaultErrorHandler);

		load(args.length > 0 ? args[0] : null);

		renderer = new RenderLoop();
		renderer.start();
		JFXWindow.create();

		renderer.close();
		System.out.print("Exiting program");
		System.exit(0);
	}

	public static void load() {
		load(filepath);
	}

	private static void load(String path) {
		filepath = path;
		timeline = new Timeline();
		File f = filepath != null ? new File(filepath, "Data") : new File("Data");
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

	public static long getFPS() {
		return renderer.getFPS();
	}

}
