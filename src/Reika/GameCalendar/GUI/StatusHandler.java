package Reika.GameCalendar.GUI;

import java.util.LinkedList;

import javafx.application.Platform;

public class StatusHandler {

	private static final LinkedList<StatusHandler> statuses = new LinkedList();
	private static final StatusTicker ticker = new StatusTicker();

	private static class StatusTicker implements Runnable {

		private boolean needsUpdate = true;

		private StatusTicker() {
			statuses.add(new StatusHandler("Idle...", -1, false));
		}

		@Override
		public void run() {
			if (!statuses.isEmpty()) {
				StatusHandler sh = statuses.getLast();
				if (sh.lifetime > 0 && System.currentTimeMillis()-sh.creation > sh.lifetime) {
					statuses.removeLast();
					needsUpdate = true;
				}
			}
			if (needsUpdate)
				this.updateStatus();
			needsUpdate = false;
			Platform.runLater(this);
		}

		private void updateStatus() {
			StatusHandler sh = statuses.getLast();
			JFXWindow.getGUI().setStatus(sh.text);
			System.out.println("Setting status to '"+sh.text+"'");
		}

	}

	static {
		if (JFXWindow.getGUI() == null) {
			throw new IllegalStateException("Tried to load status system before the gui is loaded!");
		}
		Platform.runLater(ticker);
	}

	private final String text;
	private final int lifetime;
	private final boolean killOnReplace;

	private final long creation = System.currentTimeMillis();

	private StatusHandler(String s, int l, boolean k) {
		text = s;
		lifetime = l;
		killOnReplace = k;
	}

	public static void postStatus(String s, int millis) {
		postStatus(s, millis, true);
	}

	public static void postStatus(String s, int millis, boolean killOnReplace) {
		StatusHandler sh = statuses.getLast();
		while (sh.killOnReplace) {
			statuses.removeLast();
			sh = statuses.getLast();
		}
		sh = new StatusHandler(s, millis, killOnReplace);
		statuses.addLast(sh);
		ticker.needsUpdate = true;
	}

}
