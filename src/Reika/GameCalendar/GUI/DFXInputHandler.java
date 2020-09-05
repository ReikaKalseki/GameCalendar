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

	private MovingAverage mouseVelX = new MovingAverage(10);
	private MovingAverage mouseVelY = new MovingAverage(10);

	public DFXInputHandler(DriftFXSurface n) {
		renderSurface = n;
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
			double w = renderSurface.getWidth();
			double h = renderSurface.getHeight();
			double midX = w/2;
			double midY = h/2;
			double dx = event.getX()-midX;
			double dy = event.getY()-midY;
			Main.getCalendarRenderer().handleMouse(dx*2/w, -dy*2/h);
		}
	}

}
