package Reika.GameCalendar;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.GuiSystem;
import Reika.GameCalendar.Rendering.Window;
import Reika.GameCalendar.Util.DataLoader;

public class Main {

	private static final UncaughtExceptionHandler defaultErrorHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println("Thread encountered an uncaught exception:");
			System.out.println(t.toString());
			e.printStackTrace();
		}

	};

	private static Window window;
	private static Timeline timeline;
	private static GuiSystem gui;

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(defaultErrorHandler);
		File f = new File(".");
		try {
			timeline = DataLoader.loadTimeline("Data");
		}
		catch (Exception e) {
			throw new RuntimeException("Could not load data files!", e);
		}
		timeline.prepare();
		window = new Window();
		window.create();
		gui = new GuiSystem(timeline);
		try {
			while (window.run()) {

			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		window.close();
		System.out.print("Run complete");
		System.exit(0);
	}
	/*
	public static Timeline getTimeline() {
		return timeline;
	}*/

	public static GuiSystem getGUI() {
		return gui;
	}

}
