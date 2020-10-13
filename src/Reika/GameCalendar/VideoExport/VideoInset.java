package Reika.GameCalendar.VideoExport;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Reika.GameCalendar.Util.DateStamp;

public interface VideoInset {

	public void draw(BufferedImage frame, Graphics2D graphics, Font f, DateStamp date);

	/*
	public static interface SelfRegisteringVideoInset extends VideoInset {

		public boolean shouldRegister();

	}*/

}
