package Reika.GameCalendar.GUI;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Util.MovingAverage;

@Deprecated
public class GLFWInputHandler {

	public final GLFWCursorPosCallback mouseCall = new MouseCallback();
	public final GLFWMouseButtonCallback clickCall = new ClickCallback();

	private final int screenSizeX;
	private final int screenSizeY;
	private final long windowID;

	private double mouseX;
	private double mouseY;
	private double lastMouseX;
	private double lastMouseY;

	private boolean isClick;

	private MovingAverage mouseVelX = new MovingAverage(10);
	private MovingAverage mouseVelY = new MovingAverage(10);

	public GLFWInputHandler(int width, int height, long ID) {
		screenSizeX = width;
		screenSizeY = height;
		windowID = ID;
	}

	private class MouseCallback extends GLFWCursorPosCallback {

		@Override
		public void invoke(long window, double xpos, double ypos) {
			if (window == windowID) {
				mouseX = (xpos/screenSizeX-0.5)*2;
				mouseY = -(ypos/screenSizeY-0.5)*2;
			}
		}

	}

	private class ClickCallback extends GLFWMouseButtonCallback {

		@Override
		public void invoke(long window, int button, int action, int mods) {
			if (window == windowID && action == GLFW.GLFW_PRESS) {
				GLFWInputHandler.this.handleClick(button);
				isClick = true;
			}
			else {
				isClick = false;
			}
		}

	}

	private void handleClick(int button) {
		//System.out.println(mouseX);
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.onClick(mouseX, mouseY, mouseVelX.getAverage(), mouseVelY.getAverage());
		}
	}

	private void onClick(double x, double y, double vx, double vy) {
		Main.getCalendarRenderer().handleMouse(x, y);
	}

}
