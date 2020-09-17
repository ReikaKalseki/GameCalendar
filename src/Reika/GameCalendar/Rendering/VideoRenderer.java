package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.lwjgl.opengl.GL11;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.GuiHighlight;
import Reika.GameCalendar.GUI.GuiSection;
import Reika.GameCalendar.GUI.StatusHandler;
import Reika.GameCalendar.Util.GLFunctions;
import Reika.GameCalendar.Util.TextureLoader;

public class VideoRenderer {

	public static final VideoRenderer instance = new VideoRenderer();

	private static final int CALENDAR_SIZE = 800;
	private static final int SCREENSHOT_WIDTH = 480;
	private static final int SCREENSHOT_HEIGHT = 270;
	private static final int VIDEO_WIDTH = 1760;
	private static final int VIDEO_HEIGHT = 1080;

	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;
	private AWTSequenceEncoder encoder;
	private Framebuffer renderedOutput;

	private final HashMap<String, BufferedImage> imageCache = new HashMap();
	private final HashMap<String, Integer> usedScreenshotSlots = new HashMap();
	private final HashSet<Integer> freeScreenshotSlots = new HashSet();

	private VideoRenderer() {

	}

	public void startRendering(CalendarRenderer data) {
		isRendering = true;
		renderer = data;

		time = Main.getTimeline();
		renderer.limit = time.getStart();

		StatusHandler.postStatus("Rendering video...", 999999999);
	}

	private void init() {
		try {
			encoder = AWTSequenceEncoder.createSequenceEncoder(new File("E:/videotest22b.mp4"), 60);
			if (renderedOutput == null)
				renderedOutput = new Framebuffer(VIDEO_WIDTH, VIDEO_HEIGHT);

			for (int i = 0; i < 8; i++) {
				freeScreenshotSlots.add(GL11.glGenTextures());
			}
		}
		catch (IOException e) {
			e.printStackTrace();

			StatusHandler.postStatus("Video creation failed.", 2500, false);
		}
	}

	public boolean isRendering() {
		return isRendering;
	}

	public void addFrame(Framebuffer calendar) {
		if (encoder == null)
			this.init();

		GL11.glViewport(0, 0, VIDEO_WIDTH, VIDEO_HEIGHT);

		BufferedImage frame = new BufferedImage(VIDEO_WIDTH, VIDEO_HEIGHT, BufferedImage.TYPE_INT_RGB);
		HashSet<String> usedImages = new HashSet();
		ArrayList<File> li = this.getCurrentScreenshots();
		for (int i = 0; i < li.size(); i++) {
			File img = li.get(i);
			this.drawScreenshot(img, i, usedImages);
		}
		this.cleanImageCache(usedImages);
		renderedOutput.writeIntoImage(frame, 0, 0);
		calendar.writeIntoImage(frame, 0, 0);
		try {
			//encoder.encodeImage(frame);
			if (!usedImages.isEmpty() && renderer.limit.day%4 == 0) {
				File f = new File("E:/CalendarVideoFrames/"+renderer.limit.toString().replace('/', '-')+".png");
				f.getParentFile().mkdirs();
				ImageIO.write(frame, "png", f);
				if (renderer.limit.year >= 2012)
					throw new RuntimeException("End");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video frame construction failed.", 2500, false);
			this.end();
		}

		if (renderer.limit.compareTo(time.getEnd()) >= 0) {
			this.finish();
		}
		else {
			for (int i = 0; i < 1; i++)
				renderer.limit = renderer.limit.nextDay();
		}
	}

	private void cleanImageCache(HashSet<String> usedImages) {
		Iterator<String> it = imageCache.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (!usedImages.contains(s)) {
				int gl = usedScreenshotSlots.remove(s);
				freeScreenshotSlots.add(gl);
				it.remove();
			}
		}
	}

	private void drawScreenshot(File img, int i, HashSet<String> usedImages) {
		String p = img.getAbsolutePath();
		usedImages.add(p);
		BufferedImage data = this.getOrLoadImage(p, img);
		int gl = this.getOrCreateGLTexture(p, data);
		GLFunctions.printGLErrors("Screenshot data load");
		int ox = SCREENSHOT_WIDTH*(i%2);
		int oy = SCREENSHOT_HEIGHT*(i/2);
		int x = CALENDAR_SIZE+ox;
		int y = oy;
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1, 1, 1, 1);
		GLFunctions.printGLErrors("Draw prepare");
		renderedOutput.bind(false);
		GLFunctions.printGLErrors("FB bind");
		GLFunctions.drawTextureAsQuadScreenCoords(gl, x, y, SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, VIDEO_WIDTH, VIDEO_HEIGHT);
		GLFunctions.printGLErrors("Screenshot quad draw");
		renderedOutput.unbind();
		GLFunctions.printGLErrors("FB unbind");
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GLFunctions.printGLErrors("attrib reset");
		GL11.glPopMatrix();
		GLFunctions.printGLErrors("Matrix pop");
		/*
		GLFunctions.writeTextureToImage(data, x, y, SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, gl);
		try {
			ImageIO.write(data, "png", new File("E:/videoscreenshot.png"));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("tried and failed", e);
		}
		throw new RuntimeException("tried");
		 */
	}

	private int getOrCreateGLTexture(String p, BufferedImage data) {
		Integer get = usedScreenshotSlots.get(p);
		if (get == null) {
			if (freeScreenshotSlots.isEmpty())
				throw new IllegalStateException("Screenshot ID pool exhausted!");
			int id = freeScreenshotSlots.iterator().next();
			freeScreenshotSlots.remove(id);
			get = id;
			usedScreenshotSlots.put(p, get);
			TextureLoader.instance.loadImageOntoTexture(data, id, true, true);
		}
		return get.intValue();
	}

	private BufferedImage getOrLoadImage(String p, File img) {
		BufferedImage data = imageCache.get(p);
		if (data == null) {
			try {
				data = ImageIO.read(img);
			}
			catch (IOException e) {
				throw new RuntimeException("Could not load screenshot file for video construction", e);
			}
			imageCache.put(p, data);
		}
		return data;
	}

	private void finish() {
		try {
			encoder.finish();
			StatusHandler.postStatus("Video completion succeeded.", 2500, false);
		}
		catch(Exception e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video completion failed.", 2500, false);
		}

		this.end();
	}

	private void end() {
		for (int id : freeScreenshotSlots)
			GL11.glDeleteTextures(id);
		for (int id : usedScreenshotSlots.values())
			GL11.glDeleteTextures(id);
		usedScreenshotSlots.clear();
		freeScreenshotSlots.clear();
		isRendering = false;
		renderer.limit = null;
		renderer = null;
		time = null;
		encoder = null;
		renderedOutput.clear();
	}

	private ArrayList<File> getCurrentScreenshots() {
		ArrayList<File> ret = new ArrayList();
		GuiSection s = renderer.getSectionAt(renderer.limit);
		if (s != null) {
			ArrayList<CalendarEvent> li = new ArrayList();
			li.addAll(s.getItems(false));
			Collection<GuiHighlight> c = renderer.getHighlightsInSection(s);
			for (GuiHighlight h : c) {
				li.addAll(h.getItems(false));
			}
			Collections.sort(li, CalendarRenderer.eventSorter);
			for (CalendarEvent e : li) {
				File img = e.getScreenshotFile();
				if (img != null)
					ret.add(img);
			}
		}
		return ret;
	}

}
