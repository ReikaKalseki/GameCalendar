package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
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
	private static final int VIDEO_FPS = 40;

	public static String pathToFFMPEG = "E:/My Documents/Programs and Utilities/ffmpeg-4.3.1-full_build/bin/ffmpeg.exe";

	private boolean isInitialized = false;
	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;
	private Framebuffer renderedOutput;

	private AWTSequenceEncoder encoder;

	private Process process;
	private OutputStream ffmpegDataLine;

	private final HashMap<String, BufferedImage> imageCache = new HashMap();
	private final HashMap<String, Integer> usedScreenshotSlots = new HashMap();
	private final HashSet<Integer> freeScreenshotSlots = new HashSet();
	private final ArrayList<EmbeddedEvent> currentItems = new ArrayList();
	private final EmbeddedEvent[] currentImages = new EmbeddedEvent[8];

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
			File f = new File("videotest.mp4");
			if (f.exists())
				f.delete();

			if (pathToFFMPEG != null) {
				List<String> command = this.getFFMPEGArgs(f);
				command.add(0, pathToFFMPEG);

				//command = Arrays.asList("java", "-jar", "DataDump.jar");

				ProcessBuilder builder = new ProcessBuilder(command);
				builder.redirectError(Redirect.INHERIT);
				builder.redirectOutput(Redirect.INHERIT);
				process = builder.directory(f.getParentFile()).start();

				//OutputStream exportLogOut = new FileOutputStream("videoexportffmpeg.log");
				//new StreamPipe(process.getInputStream(), exportLogOut).start();
				//new StreamPipe(process.getErrorStream(), exportLogOut).start();

				ffmpegDataLine = process.getOutputStream();
			}
			else {
				encoder = AWTSequenceEncoder.createSequenceEncoder(f, 60);
			}
			if (renderedOutput == null)
				renderedOutput = new Framebuffer(VIDEO_WIDTH, VIDEO_HEIGHT);

			for (int i = 0; i < 8; i++) {
				freeScreenshotSlots.add(GL11.glGenTextures());
			}

			isInitialized = true;
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
		try {
			if (!isInitialized)
				this.init();

			GL11.glViewport(0, 0, VIDEO_WIDTH, VIDEO_HEIGHT);
			renderedOutput.clear(1, 1, 1);

			BufferedImage frame = new BufferedImage(VIDEO_WIDTH, VIDEO_HEIGHT, BufferedImage.TYPE_INT_RGB);
			HashSet<String> usedImages = new HashSet();
			ArrayList<CalendarEvent> li = this.getCurrentItems();

			ArrayList<EmbeddedEvent> toRemove = new ArrayList();

			HashSet<CalendarEvent> newEntries = new HashSet(li);
			for (EmbeddedEvent e : currentItems) {
				if (li.contains(e.event)) {
					e.age++;
					newEntries.remove(e.event);
				}
				else {
					toRemove.add(e);
				}
			}
			for (EmbeddedEvent e : toRemove) {
				currentItems.remove(e);
				if (e.slotIndex >= 0)
					currentImages[e.slotIndex] = null;
			}
			for (CalendarEvent e : newEntries) {
				int i = e.getScreenshotFile() != null ? this.getFirstFreeImageSlot() : -1;
				EmbeddedEvent ee = new EmbeddedEvent(e, i);
				if (i >= 0)
					currentImages[i] = ee;
				currentItems.add(ee);
			}
			for (EmbeddedEvent e : currentItems) {
				if (e.hasImage())
					usedImages.add(this.drawScreenshot(e));
			}
			//System.out.println("Frame "+renderer.limit.toString()+" used screenshots: "+usedImages);
			this.cleanImageCache(usedImages);
			renderedOutput.writeIntoImage(frame, 0, 0);
			calendar.writeIntoImage(frame, 0, 0);
			int n = !newEntries.isEmpty() ? 30 : 1;
			if (pathToFFMPEG != null) {
				ByteBuffer buf = bufferize(frame);
				for (int i = 0; i < n; i++) {
					for (int a = 0; a < buf.limit(); a++)
						ffmpegDataLine.write(buf.get());
					buf.rewind();
				}
			}
			else {
				Picture p = AWTUtil.fromBufferedImageRGB(frame);
				for (int i = 0; i < n; i++)
					encoder.encodeNativeFrame(p);
				//encoder.encodeImage(frame);
			}


			if (!usedImages.isEmpty() && (renderer.limit.day%4 == 0 || !newEntries.isEmpty())) {
				File f = new File("E:/CalendarVideoFrames/"+renderer.limit.toString().replace('/', '-')+".png");
				f.getParentFile().mkdirs();
				ImageIO.write(frame, "png", f);
				if (renderer.limit.year >= 2012)
					throw new RuntimeException("End");
			}

			if (renderer.limit.compareTo(time.getEnd()) >= 0) {
				this.finish();
			}
			else {
				//for (int i = 0; i < 500; i++)
				renderer.limit = renderer.limit.nextDay();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video frame construction failed.", 2500, false);
			this.finish();
		}
	}

	private int getFirstFreeImageSlot() {
		for (int i = 0; i < currentImages.length; i++) {
			if (currentImages[i] == null)
				return i;
		}
		throw new RuntimeException("Exhausted image slots!");
	}

	private void cleanImageCache(HashSet<String> usedImages) {
		Iterator<String> it = imageCache.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (!usedImages.contains(s)) {
				int gl = usedScreenshotSlots.remove(s);
				freeScreenshotSlots.add(gl);
				it.remove();
				System.out.println("Removed screenshot "+s+" bound to GL ID "+gl);
			}
		}
	}

	private String drawScreenshot(EmbeddedEvent e) {
		File img = e.event.getScreenshotFile();
		String p = img.getAbsolutePath();
		BufferedImage data = this.getOrLoadImage(p, img);
		int gl = this.getOrCreateGLTexture(p, data);
		GLFunctions.printGLErrors("Screenshot data load");
		int ox = SCREENSHOT_WIDTH*(e.slotIndex%2);
		int oy = SCREENSHOT_HEIGHT*(e.slotIndex/2);
		int x = CALENDAR_SIZE+ox;
		int y = oy;
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		float f = e.age >= 5 ? 1 : (e.age+1)/5F;
		GL11.glColor4f(1, 1, 1, f);
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
		return p;
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
			try {
				TextureLoader.instance.loadImageOntoTexture(data, id, true, true);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not load texture "+p, e);
			}
			System.out.println("Assigned image from "+p+" onto GL ID "+id);
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
			System.out.println("Loaded image data from "+p);
		}
		return data;
	}

	private void finish() {
		try {
			if (pathToFFMPEG != null) {
				ffmpegDataLine.close();
				int code = process.waitFor();
				if (code != 0) {
					throw new RuntimeException("Process encountered error code: "+code);
				}
			}
			else {
				encoder.finish();
			}
			StatusHandler.postStatus("Video completion succeeded.", 2500, false);
		}
		catch (Exception e) {
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
		currentItems.clear();
		for (int i = 0; i < currentImages.length; i++) {
			currentImages[i] = null;
		}
		isRendering = false;
		renderer.limit = null;
		renderer = null;
		time = null;
		encoder = null;
		ffmpegDataLine = null;
		process = null;
		isInitialized = false;
		renderedOutput.clear();
	}

	private ArrayList<CalendarEvent> getCurrentItems() {
		ArrayList<File> ret = new ArrayList();
		GuiSection s = renderer.getSectionAt(renderer.limit);
		ArrayList<CalendarEvent> li = new ArrayList();
		if (s != null && !s.section.isEmpty()) {
			li.addAll(s.getItems(false));/*
			Collection<GuiHighlight> c = renderer.getHighlightsInSection(s);
			for (GuiHighlight h : c) {
				li.addAll(h.getItems(false));
			}*/
		}
		//else {
		GuiHighlight h = renderer.getHighlightAtDate(renderer.limit);
		if (h != null) {
			li.addAll(h.getItems(false));
		}
		//}
		return li;
	}

	private static List<String> getFFMPEGArgs(File f) {
		List<String> parts = new ArrayList(Arrays.asList(("-f rawvideo -pix_fmt 0rgb -s:v "+VIDEO_WIDTH+"x"+VIDEO_HEIGHT+" -r "+VIDEO_FPS+" -i pipe: -c:v libx264").split(" ")));
		parts.add("\""+f.getAbsolutePath()+"\"");
		return parts;
	}

	private static ByteBuffer bufferize(BufferedImage img) {
		ByteBuffer byteBuffer;
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();

		if (dataBuffer instanceof DataBufferByte) {
			byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
			byteBuffer = ByteBuffer.wrap(pixelData);
		}
		else if (dataBuffer instanceof DataBufferUShort) {
			short[] pixelData = ((DataBufferUShort) dataBuffer).getData();
			byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
			byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
		}
		else if (dataBuffer instanceof DataBufferShort) {
			short[] pixelData = ((DataBufferShort) dataBuffer).getData();
			byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
			byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
		}
		else if (dataBuffer instanceof DataBufferInt) {
			int[] pixelData = ((DataBufferInt) dataBuffer).getData();
			byteBuffer = ByteBuffer.allocate(pixelData.length * 4);
			byteBuffer.asIntBuffer().put(IntBuffer.wrap(pixelData));
		}
		else {
			throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
		}
		return byteBuffer;
	}

	private static class StreamPipe extends Thread {

		private InputStream inputStream;
		private OutputStream outputStream;

		public StreamPipe(InputStream in, OutputStream out) {
			inputStream = in;
			outputStream = out;
		}

		@Override
		public void run() {
			try {
				IOUtils.copy(inputStream, outputStream);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class EmbeddedEvent implements Comparable<EmbeddedEvent> {

		private final CalendarEvent event;
		private final int slotIndex;

		private int age = 0;

		private EmbeddedEvent(CalendarEvent ce, int idx) {
			event = ce;
			slotIndex = idx;
		}

		@Override
		public int compareTo(EmbeddedEvent o) {
			return CalendarRenderer.eventSorter.compare(event, o.event);
		}

		public boolean hasImage() {
			return event.getScreenshotFile() != null;
		}

	}

}
