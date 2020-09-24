package Reika.GameCalendar.GUI;

import Reika.GameCalendar.Util.MathHelper;

import javafx.scene.control.Slider;
import javafx.util.StringConverter;


public class NonlinearSlider extends Slider {

	private final double[] options;

	public NonlinearSlider(Slider root, double[] arr, boolean fractionalize) {
		options = arr;
		this.setNodeOrientation(root.getNodeOrientation());
		this.setOrientation(root.getOrientation());
		this.setPrefSize(root.getPrefWidth(), root.getPrefHeight());
		this.setRotate(root.getRotate());
		//this.setSkin(root.getSkin());
		//this.setStyle(root.getStyle());
		this.setShowTickMarks(root.showTickMarksProperty().get());
		this.setShowTickLabels(root.showTickLabelsProperty().get());
		this.setTooltip(root.getTooltip());
		this.setVisible(root.visibleProperty().get());

		this.setSnapToTicks(true);
		this.setMajorTickUnit(1);
		this.setMinorTickCount(0);
		this.setMin(0);
		this.setMax(options.length-1);

		this.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double val) {
				int index = (int)Math.round(val);
				double sp = options[index];
				String s = String.valueOf(sp);
				if (fractionalize) {
					s = MathHelper.fractionalize(sp);
				}
				return s;
			}

			@Override
			public Double fromString(String s) {
				return Double.parseDouble(s);
			}
		});
	}

}
