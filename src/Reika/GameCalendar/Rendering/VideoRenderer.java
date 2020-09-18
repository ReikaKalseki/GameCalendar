package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	public static String pathToFFMPEG = "E:/My Documents/Programs and Utilities/ffmpeg-4.3.1-full_build/bin/ffmpeg.exe";

	private boolean isInitialized = false;
	private boolean isRendering;
	private CalendarRenderer renderer;
	private Timeline time;
	private Framebuffer renderedOutput;

	private AWTSequenceEncoder encoder;

	private Process process;
	private WritableByteChannel ffmpegDataLine;

	private final HashMap<String, BufferedImage> imageCache = new HashMap();
	private final HashMap<String, Integer> usedScreenshotSlots = new HashMap();
	private final HashSet<Integer> freeScreenshotSlots = new HashSet();

	private HashSet<CalendarEvent> lastItems = null;

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

				process = new ProcessBuilder(command).directory(f.getParentFile()).start();

				OutputStream exportLogOut = new FileOutputStream("videoexportffmpeg.log");
				new StreamPipe(process.getInputStream(), exportLogOut).start();
				new StreamPipe(process.getErrorStream(), exportLogOut).start();

				OutputStream o = process.getOutputStream();
				ffmpegDataLine = Channels.newChannel(o);
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
			for (int i = 0; i < li.size(); i++) {
				File img = li.get(i).getScreenshotFile();
				if (img != null)
					usedImages.add(this.drawScreenshot(img, i));
			}
			//System.out.println("Frame "+renderer.limit.toString()+" used screenshots: "+usedImages);
			this.cleanImageCache(usedImages);
			renderedOutput.writeIntoImage(frame, 0, 0);
			calendar.writeIntoImage(frame, 0, 0);
			HashSet<CalendarEvent> newEntries = new HashSet(li);
			if (lastItems != null)
				newEntries.removeAll(lastItems);
			lastItems = new HashSet(li);
			int n = !newEntries.isEmpty() ? 30 : 1;
			if (pathToFFMPEG != null) {
				for (int i = 0; i < n; i++)
					ffmpegDataLine.write(bufferize(frame));
			}
			else {
				Picture p = AWTUtil.fromBufferedImageRGB(frame);
				for (int i = 0; i < n; i++)
					encoder.encodeNativeFrame(p);
				//encoder.encodeImage(frame);
			}

			/*
			if (!usedImages.isEmpty() && (renderer.limit.day%4 == 0 || !newEntries.isEmpty())) {
				File f = new File("E:/CalendarVideoFrames/"+renderer.limit.toString().replace('/', '-')+".png");
				f.getParentFile().mkdirs();
				ImageIO.write(frame, "png", f);
				if (renderer.limit.year >= 2017)
					throw new RuntimeException("End");
			}
			 */

			if (renderer.limit.compareTo(time.getEnd()) >= 0) {
				this.finish();
			}
			else {
				for (int i = 0; i < 500; i++)
					renderer.limit = renderer.limit.nextDay();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video frame construction failed.", 2500, false);
			this.finish();
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
				System.out.println("Removed screenshot "+s+" bound to GL ID "+gl);
			}
		}
	}

	private String drawScreenshot(File img, int i) {
		String p = img.getAbsolutePath();
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
		lastItems = null;
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
		Collections.sort(li, CalendarRenderer.eventSorter);
		return li;
	}

	private static List<String> getFFMPEGArgs(File f) {
		List<String> parts = new ArrayList(Arrays.asList(("-f rawvideo -pix_fmt rgb24 -s:v "+VIDEO_WIDTH+"x"+VIDEO_HEIGHT+" -r 25 -i pipe: -c:v libx264").split(" ")));
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

	private static class EmbeddedEvent {

		private final CalendarEvent event;

		private EmbeddedEvent(CalendarEvent ce) {
			event = ce;
		}

	}

}
