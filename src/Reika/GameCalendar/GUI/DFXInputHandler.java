package Reika.GameCalendar.GUI;

import org.eclipse.fx.drift.DriftFXSurface;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Util.MovingAverage;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DFXInputHandler implements EventHandler<MouseEvent> {

	private final DriftFXSurface renderSurface;

	private double mouseX;
	private double mouseY;
	private double lastMouseX;
	private double lastMouseY;

	private double JFXmouseX;
	private double JFXmouseY;

	private MovingAverage mouseVelX = new MovingAverage(10);
	private MovingAverage mouseVelY = new MovingAverage(10);

	public DFXInputHandler(DriftFXSurface n) {
		renderSurface = n;
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
			mouseX = this.convertX(event.getX());
			mouseY = this.convertY(event.getY());
			Main.getCalendarRenderer().handleMouse(mouseX, mouseY);
		}
		if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			lastMouseX = mouseX;
			lastMouseY = mouseY;
			mouseX = this.convertX(event.getX());
			mouseY = this.convertY(event.getY());
			mouseVelX.addValue(mouseX-lastMouseX);
			mouseVelY.addValue(mouseY-lastMouseY);
			JFXmouseX = event.getX();
			JFXmouseY = event.getY();
		}
	}

	private double convertX(double x) {
		double w = renderSurface.getWidth();
		double midX = w/2;
		double dx = x-midX;
		return dx*2/w;
	}

	private double convertY(double y) {
		double h = renderSurface.getHeight();
		double midY = h/2;
		double dy = y-midY;
		return -dy*2/h;
	}

	public double getMouseX(boolean jfx) {
		return jfx ? JFXmouseX : mouseX;
	}

	public double getMouseY(boolean jfx) {
		return jfx ? JFXmouseY : mouseY;
	}

}
