package Reika.GameCalendar.Rendering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.awt.GLData.Profile;

import com.google.common.base.Throwables;

import Reika.GameCalendar.Main;

public class Window implements Runnable {

	public static final int MAX_FPS = 60;
	private static final int MILLIS_PER_FRAME = 1000/MAX_FPS;

	public static final int BORDER_X = 17; //windows border thickness
	public static final int BORDER_Y = 39;

	//private long windowID;

	private boolean shouldClose = false;

	private int screenSizeX = 800;
	private int screenSizeY = 800;

	private final AWTGLCanvas canvas;

	//private GLFWFramebufferSizeCallback resizeCall;

	public Window() {
		/*
		frame = new Frame("Program Window");
		frame.setLayout(new BorderLayout());
		canvas = new Canvas();
		frame.add(canvas, BorderLayout.CENTER);
		 */
		JFrame frame = new JFrame("AWT test");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		GLData data = new GLData();
		data.samples = 4;
		data.profile = Profile.COMPATIBILITY;
		data.majorVersion = 3;
		data.minorVersion = 2;
		canvas = new WindowCanvas(data);
		frame.add(canvas, BorderLayout.CENTER);

		frame.setPreferredSize(new Dimension(800+BORDER_X, 800+BORDER_Y));
		frame.setMinimumSize(new Dimension(400+BORDER_X, 400+BORDER_Y));
		frame.pack();
		frame.setVisible(true);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);

		frame.addWindowListener(new MyWindowListener());
		frame.transferFocus();

		GLFW.glfwInit();
	}

	public void create() {
		try {
			/*
			Display.setFullscreen(false);
			Display.setParent(canvas);
			Display.setResizable(true);
			Display.create();
			Display.setVSyncEnabled(true);
			 */
			this.setupGL();
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	private void setupGL() {
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		/*
		windowID = GLFW.glfwCreateWindow(screenSizeX, screenSizeY, "Game Calendar", 0, 0);
		if (windowID == 0) {
			throw new RuntimeException("Failed to create window");
		}
		GLFW.glfwMakeContextCurrent(windowID);
		 */
		GL.createCapabilities();
		//GLFW.glfwShowWindow(windowID);

		//resizeCall = new ReSizeCallback();

		//GLFW.glfwSetFramebufferSizeCallback(windowID, resizeCall);
		GL11.glClearColor(1, 1, 1, 1);
	}

	private class WindowCanvas extends AWTGLCanvas {

		public WindowCanvas(GLData data) {
			super(data);
		}

		@Override
		public void initGL() {
			Window.this.setupGL();
			//GLFW.glfwHideWindow(windowID);
		}

		@Override
		public void paintGL() {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			Window.this.drawGUI();
			//GLFW.glfwPollEvents();
			//GLFW.glfwSwapBuffers(windowID);
			this.swapBuffers();

			GL11.glViewport(0, 0, screenSizeX, screenSizeY);
		}

	}
	/*
	private class ReSizeCallback extends GLFWFramebufferSizeCallback {

		@Override
		public void invoke(long window, int width, int height) {
			if (window == windowID) {
				screenSizeX = width;
				screenSizeY = height;
			}
		}

	}
	 */
	public void close() {
		//GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
	}

	public void run() {
		long pre = System.currentTimeMillis();
		canvas.render();
		long post = System.currentTimeMillis();
		long sleep = MILLIS_PER_FRAME-(post-pre);
		if (sleep > 0) {
			try {
				Thread.sleep(sleep);
			}
			catch (InterruptedException e) {
				shouldClose = true;
				e.printStackTrace();
			}
		}
	}

	public boolean shouldClose() {
		return shouldClose;// || GLFW.glfwWindowShouldClose(windowID);
	}

	private void drawGUI() {
		Main.getGUI().draw(screenSizeX, screenSizeY);
		Main.getGUI().handleMouse(screenSizeX, screenSizeY);
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
