package Reika.GameCalendar.Rendering;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;

import Reika.GameCalendar.Main;

public class Window {

	public static final int MAX_FPS = 60;
	private static final int MILLIS_PER_FRAME = 1000/MAX_FPS;

	public static final int BORDER_X = 17; //windows border thickness
	public static final int BORDER_Y = 39;

	private final Frame frame;
	private final Canvas canvas;

	private boolean shouldClose = false;

	public Window() {
		frame = new Frame("Program Window");
		frame.setLayout(new BorderLayout());
		canvas = new Canvas();
		frame.add(canvas, BorderLayout.CENTER);

		frame.setPreferredSize(new Dimension(800+BORDER_X, 800+BORDER_Y));
		frame.setMinimumSize(new Dimension(400+BORDER_X, 400+BORDER_Y));
		frame.pack();
		frame.setVisible(true);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);

		frame.addWindowListener(new MyWindowListener());
	}

	public void create() {
		try {
			Display.setFullscreen(false);
			Display.setParent(canvas);
			Display.setResizable(true);
			Display.create();
			Display.setVSyncEnabled(true);
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	public void close() {
		Display.destroy();
		frame.dispose();
	}

	public boolean run() throws InterruptedException {
		long pre = System.currentTimeMillis();
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		this.drawGUI();
		Display.update();
		if (Display.wasResized()) {
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			frame.repaint();
			frame.revalidate();
		}
		long post = System.currentTimeMillis();
		long sleep = MILLIS_PER_FRAME-(post-pre);
		if (sleep > 0) {
			Thread.sleep(sleep);
		}
		return !shouldClose && !Display.isCloseRequested();
	}

	private void drawGUI() {
		Main.getGUI().draw(frame.getSize());
		Main.getGUI().handleMouse(frame.getSize());
	}

	private class MyWindowListener implements WindowListener {

		public void windowClosing(WindowEvent arg0) {
			shouldClose = true;
		}

		public void windowOpened(WindowEvent arg0) {}
		public void windowClosed(WindowEvent arg0) {}
		public void windowIconified(WindowEvent arg0) {}
		public void windowDeiconified(WindowEvent arg0) {}
		public void windowActivated(WindowEvent arg0) {}
		public void windowDeactivated(WindowEvent arg0) {}

	}

}
