package Reika.GameCalendar.Rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;

import Reika.GameCalendar.Main;

public class GLFWWindow {

	public static final String PROGRAM_TITLE = "Game Calendar";

	public static final int MAX_FPS = 60;
	public static final int MILLIS_PER_FRAME = 1000/MAX_FPS;

	public static final int BORDER_X = 17; //windows border thickness
	public static final int BORDER_Y = 39;

	private long windowID;

	private boolean shouldClose = false;

	private int screenSizeX = 800;
	private int screenSizeY = 800;

	private GLFWFramebufferSizeCallback resizeCall;

	public GLFWWindow() {
		/*
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
		 */
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
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
			GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 4);
			GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
			windowID = GLFW.glfwCreateWindow(screenSizeX, screenSizeY, PROGRAM_TITLE, 0, 0);
			if (windowID == 0) {
				throw new RuntimeException("Failed to create window");
			}
			GLFW.glfwMakeContextCurrent(windowID);
			GL.createCapabilities();
			GLFW.glfwShowWindow(windowID);

			resizeCall = new ReSizeCallback();

			GLFW.glfwSetFramebufferSizeCallback(windowID, resizeCall);
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	private class ReSizeCallback extends GLFWFramebufferSizeCallback {

		@Override
		public void invoke(long window, int width, int height) {
			if (window == windowID) {
				screenSizeX = width;
				screenSizeY = height;
			}
		}

	}

	public void close() {
		GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
	}

	public boolean run() throws InterruptedException {
		long pre = System.currentTimeMillis();
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		this.drawGUI();
		GLFW.glfwPollEvents();
		GLFW.glfwSwapBuffers(windowID);

		GL11.glViewport(0, 0, screenSizeX, screenSizeY);
		long post = System.currentTimeMillis();
		long sleep = MILLIS_PER_FRAME-(post-pre);
		if (sleep > 0) {
			Thread.sleep(sleep);
		}
		return !shouldClose && !GLFW.glfwWindowShouldClose(windowID);
	}

	private void drawGUI() {
		Main.getCalendarRenderer().draw(screenSizeX, screenSizeY);
		Main.getCalendarRenderer().handleMouse(screenSizeX, screenSizeY);
	}
	/*
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

	}*/

}
