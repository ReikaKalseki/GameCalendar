package Reika.GameCalendar.VideoExport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.lwjgl.opengl.GL11;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.Data.ActivityCategory;
import Reika.GameCalendar.Data.ActivityCategory.SortingMode;
import Reika.GameCalendar.Data.ActivityValue;
import Reika.GameCalendar.Data.CalendarEvent;
import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.GuiController.GuiElement;
import Reika.GameCalendar.GUI.GuiHighlight;
import Reika.GameCalendar.GUI.GuiSection;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.StatusHandler;
import Reika.GameCalendar.IO.TextureLoader;
import Reika.GameCalendar.Rendering.CalendarRenderer;
import Reika.GameCalendar.Rendering.Framebuffer;
import Reika.GameCalendar.Rendering.RenderLoop;
import Reika.GameCalendar.Util.DateStamp;
import Reika.GameCalendar.Util.GLFunctions;
import Reika.GameCalendar.Util.GLFunctions.BlendMode;

public class VideoRenderer {

	public static final VideoRenderer instance = new VideoRenderer();

	public static final int CALENDAR_SIZE = 800;
	public static final int SCREENSHOT_WIDTH = 480;
	public static final int SCREENSHOT_HEIGHT = 270;
	public static final int VIDEO_WIDTH = 1920;
	public static final int VIDEO_HEIGHT = 1080;
	private static final int VIDEO_FPS = 40;
	//private static final int PORT_NUMBER = 22640;
	//private static final int FADEOUT_DAYS = 0;
	private static final double GAMMA = 1.02;

	private final ArrayList<VideoInset> videoInsets = new ArrayList();
	private int skipSpeed = 1;

	public String pathToFFMPEG = null;
	public double daysPerFrame = 1;
	public int pauseDuration = 2;
	public DateStamp startDate = null;
	public DateStamp endDate = null;
	public VideoFormats videoFormat = VideoFormats.X264;
	public String outputPath = null;
	public int maxSkipSpeed = 5;
	public double finalHoldTime = 0;

	private static final Comparator<EmbeddedEvent> embedByCategory = new Comparator<EmbeddedEvent>() {

		@Override
		public int compare(EmbeddedEvent o1, EmbeddedEvent o2) {
			SortingMode m = JFXWindow.getSortingMode();
			if (m == SortingMode.INDIVIDUAL) {
				return CalendarRenderer.eventSorter.compare(o1.event, o2.event);
			}
			int cat = o1.event.category.compareTo(m, o2.event.category);
			return cat != 0 ? cat : CalendarRenderer.eventSorter.compare(o1.event, o2.event);
		}

	};

	private boolean isInitialized = false;
	private boolean isRendering;
	private CalendarRenderer renderer;
	private Framebuffer renderedOutput;

	private boolean flipBuffers = false;
	private int exportedFrames = 0;

	private AWTSequenceEncoder encoder;

	private Process process;
	private OutputStream ffmpegDataLine;

	private final HashMap<String, BufferedImage> imageCache = new HashMap();
	private final HashMap<String, Integer> usedScreenshotSlots = new HashMap();
	private final HashSet<Integer> freeScreenshotSlots = new HashSet();
	private final ArrayList<EmbeddedEvent> currentItems = new ArrayList();
	private final EmbeddedEvent[] currentImages = new EmbeddedEvent[8];
	private final HashSet<ActivityCategory> activeCategories = new HashSet();
	private ActivityEntry[] categories = null;

	private VideoRenderer() {

	}

	public void addInset(VideoInset vi) {
		videoInsets.add(vi);
	}

	public void startRendering(CalendarRenderer data) {
		isRendering = true;
		renderer = data;

		renderer.limitEnd = startDate;

		StatusHandler.postStatus("Rendering video...", 999999999);
	}

	private void init() {
		try {
			Timeline t = Main.getTimeline();
			if (startDate == null || startDate.year < t.getStart().year)
				throw new IllegalArgumentException("Invalid start date!");
			if (endDate == null || endDate.year > t.getEnd().year)
				throw new IllegalArgumentException("Invalid end date!");

			if (outputPath.isEmpty() || outputPath.endsWith("/"))
				throw new IllegalArgumentException("Invalid output path!");

			File f = new File(outputPath+"."+videoFormat.fileExtension);
			f.getParentFile().mkdirs();
			if (f.exists())
				f.delete();

			if (pathToFFMPEG != null) {
				File exe = pathToFFMPEG.isEmpty() ? null : new File(pathToFFMPEG);
				if (exe == null || !exe.exists())
					throw new IllegalArgumentException(pathToFFMPEG.isEmpty() ? "FFMPEG path empty" : "No FFMPEG at '"+pathToFFMPEG+"'");

				List<String> command = this.getFFMPEGArgs(f);
				command.add(0, pathToFFMPEG);
				if (false) {
					command.clear();
					command.add("cmd.exe");
					command.add("/C");
					command.add("\""+command.get(2).replace('\\', '/')+"\"");
				}

				//command = Arrays.asList("java", "-jar", "DataDump.jar");

				ProcessBuilder builder = new ProcessBuilder(command);
				builder.redirectError(Redirect.INHERIT);
				builder.redirectOutput(Redirect.INHERIT);
				process = builder.directory(f.getParentFile()).start();
				ffmpegDataLine = process.getOutputStream();

				//OutputStream exportLogOut = new FileOutputStream("videoexportffmpeg.log");
				//new StreamPipe(process.getInputStream(), exportLogOut).start();
				//new StreamPipe(process.getErrorStream(), exportLogOut).start();

				//Socket s = new Socket("localhost", PORT_NUMBER);
				//ffmpegDataLine = s.getOutputStream();
			}
			else {
				encoder = AWTSequenceEncoder.createSequenceEncoder(f, 60);
			}
			if (renderedOutput == null)
				renderedOutput = new Framebuffer(VIDEO_WIDTH, VIDEO_HEIGHT).setClear(1, 1, 1);

			for (int i = 0; i < 8; i++) {
				freeScreenshotSlots.add(GL11.glGenTextures());
			}

			List<String> li = ActivityCategory.getSortedNameList(JFXWindow.getSortingMode());
			Iterator<String> it = li.iterator();
			while (it.hasNext()) {
				String s = it.next();
				if (!GuiElement.CATEGORIES.isStringSelected(s))
					it.remove();
			}
			categories = new ActivityEntry[li.size()];
			for (int i = 0; i < li.size(); i++) {
				ActivityEntry ae = new ActivityEntry(ActivityCategory.getByName(li.get(i)), i);
				ae.isActive = false;
				categories[ae.index] = ae;
			}

			isInitialized = true;
		}
		catch (Exception e) {
			e.printStackTrace();

			StatusHandler.postStatus("Video creation failed: "+e.getLocalizedMessage(), 2500, false);

			this.end();
		}
	}

	public boolean isRendering() {
		return isRendering;
	}

	public synchronized void addFrame(Framebuffer calendar) {
		if (!isInitialized)
			this.init();
		if (!isInitialized)
			return;
		try {
			GL11.glViewport(0, 0, VIDEO_WIDTH, VIDEO_HEIGHT);
			renderedOutput.clear();
			exportedFrames++;
			if (exportedFrames == 1) //first rendered frame from calendar is always garbage, holding "leftover" data
				return;

			BufferedImage frame = new BufferedImage(VIDEO_WIDTH, VIDEO_HEIGHT, BufferedImage.TYPE_INT_RGB);
			HashSet<String> usedImages = new HashSet();
			HashSet<CalendarEvent> li = this.getCurrentItems();

			ArrayList<EmbeddedEvent> toRemove = new ArrayList();

			HashSet<CalendarEvent> newEntries = new HashSet(li);
			for (EmbeddedEvent e : currentItems) {
				if (li.contains(e.event)) {
					e.age++;
					newEntries.remove(e.event);
				}
				else {
					e.holdover++;
					//if (e.holdover >= FADEOUT_DAYS)
					toRemove.add(e);
				}
			}
			for (EmbeddedEvent e : toRemove) {
				currentItems.remove(e);
				if (e.slotIndex >= 0)
					currentImages[e.slotIndex] = null;
			}
			ArrayList<CalendarEvent> newEntriesList = new ArrayList(newEntries);
			Collections.sort(newEntriesList, CalendarRenderer.eventSorter);
			for (CalendarEvent e : newEntriesList) {
				int i = e.getScreenshotFile() != null ? this.getFirstFreeImageSlot() : -1;
				EmbeddedEvent ee = new EmbeddedEvent(e, i);
				if (i >= 0)
					currentImages[i] = ee;
				currentItems.add(ee);
			}
			activeCategories.clear();
			for (EmbeddedEvent e : currentItems) {
				activeCategories.add(e.event.category);
				if (e.hasImage())
					usedImages.add(this.drawScreenshot(e));
			}
			//System.out.println("Frame "+renderer.limit.toString()+" used screenshots: "+usedImages);
			this.cleanImageCache(usedImages);
			renderedOutput.writeIntoImage(frame, 0, 0, flipBuffers);
			calendar.writeIntoImage(frame, 0, 0, flipBuffers);
			flipBuffers = !flipBuffers;
			this.addText(frame);
			int n = !newEntries.isEmpty() && pauseDuration > 0 ? VIDEO_FPS*pauseDuration : (int)Math.max(1, 1/daysPerFrame);
			this.exportFrame(frame, n);

			/*
			if (!usedImages.isEmpty() && (renderer.limit.day%4 == 0 || !newEntries.isEmpty())) {
				File f = new File("E:/CalendarVideoFrames/"+renderer.limit.toString().replace('/', '-')+".png");
				f.getParentFile().mkdirs();
				ImageIO.write(frame, "png", f);
				//if (renderer.limit.year >= 2012)
				if (Main.getTimeline().getStart().countDaysAfter(renderer.limit) > 10)
					throw new RuntimeException("End");
			}
			 */

			if (renderer.limitEnd.compareTo(endDate) >= 0) {
				if (finalHoldTime > 0) {
					int rep = (int)(finalHoldTime*VIDEO_FPS);
					this.exportFrame(frame, rep);
				}
				this.finish();
			}
			else {/*
				int nd = activeCategories.isEmpty() ? 5 : 1;
				if (renderer.limit.year >= 2013)
					nd = 100;
				if (renderer.limit.year >= 2017)
					nd = 400;*/
				int run = this.getRunSpeed();
				int rs = Math.max(1, Math.min(skipSpeed, run));
				if (run > 1) {
					skipSpeed = Math.min(skipSpeed+1, maxSkipSpeed);
				}
				else {
					skipSpeed = 1;
				}
				int step = rs*(int)Math.max(1, daysPerFrame);
				for (int i = 0; i < step; i++) {
					renderer.limitEnd = renderer.limitEnd.nextDay();
				}
			}

			if (exportedFrames == 1 || exportedFrames%(VIDEO_FPS/4) == 0)
				;//StatusHandler.postStatus("Rendering Video; Exported frame "+exportedFrames, 5000, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			StatusHandler.postStatus("Video frame construction failed.", 2500, false);
			this.finish();
		}
	}

	private int getRunSpeed() {
		//if (skipsThisFrame > skipSpeed)
		//	return false;
		if (maxSkipSpeed <= 0)
			return 0;
		HashSet<ActivityCategory> set = ActivityCategory.getActiveCategories();
		if (set.size() != 1)
			return 0;
		ActivityCategory a = set.iterator().next();
		ActivityValue av = Main.getTimeline().getActivityValue(a);
		if (av.isActiveAt(renderer.limitEnd.previousDay()) || av.isActiveAt(renderer.limitEnd) || av.isActiveAt(renderer.limitEnd.nextDay()))
			return 0;
		//skipsThisFrame++;
		//if (skipsThisFrame == skipSpeed)
		//	skipSpeed = Math.min(10, skipSpeed+1);

		DateStamp prev = av.getLastActiveDateBefore(renderer.limitEnd);
		int before = prev == null ? Integer.MAX_VALUE : prev.countDaysAfter(renderer.limitEnd);
		if (before < 20)
			return 0;

		DateStamp next = av.getNextActiveDateAfter(renderer.limitEnd);
		int after = next == null ? Integer.MAX_VALUE : renderer.limitEnd.countDaysAfter(next);
		if (after < 5)
			return 0;
		return (after-5)/2;
	}

	private void exportFrame(BufferedImage frame, int n) throws IOException {
		if (pathToFFMPEG != null) {
			ByteBuffer buf = bufferize(frame);
			for (int i = 0; i < n; i++) {
				byte[] data = new byte[buf.limit()];
				buf.get(data);
				ffmpegDataLine.write(data);
				buf.rewind();
			}
		}
		else {
			Picture p = AWTUtil.fromBufferedImageRGB(frame);
			for (int i = 0; i < n; i++)
				encoder.encodeNativeFrame(p);
			//encoder.encodeImage(frame);
		}
	}

	private void addText(BufferedImage frame) {
		Graphics2D g = (Graphics2D)frame.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(0x000000));
		g.setStroke(new BasicStroke(2F));
		Font f = g.getFont();
		int size = f.getSize();
		this.addCategoryList(g, f, size);
		this.addImageLabels(g, f, size);
		this.addDescriptionText(g, f);
		this.addCalendarLabels(g, f, size);
		for (VideoInset vi : videoInsets) {
			vi.draw(frame, g, f, renderer.limitEnd);
		}
		g.dispose();
	}

	private void addImageLabels(Graphics2D g, Font f, int size) {
		Font f2 = new Font(f.getName(), Font.BOLD, size*5/4);
		Color old = g.getColor();
		g.setColor(new Color(0xffffff));
		for (int i = 0; i < currentImages.length; i++) {
			EmbeddedEvent ee = currentImages[i];
			if (ee != null) {
				int ox = SCREENSHOT_WIDTH*(ee.slotIndex%2);
				int oy = SCREENSHOT_HEIGHT*(ee.slotIndex/2);
				int x = CALENDAR_SIZE+ox;
				int y = oy+4;
				int d = 6;
				this.drawStringWithOutline(g, f2, ee.event.category.name+": "+ee.event.name, x+d, y+d*2);
			}
		}
		g.setColor(old);
		g.setFont(f);
	}

	private void drawStringWithOutline(Graphics2D g, Font f, String s, int x, int y) {
		AffineTransform tr = g.getTransform();
		AffineTransform tr2 = (AffineTransform)tr.clone();
		tr2.translate(x, y);
		g.transform(tr2);
		g.setColor(Color.black);
		FontRenderContext frc = g.getFontRenderContext();
		TextLayout tl = new TextLayout(s, f, frc);
		Shape shape = tl.getOutline(null);
		g.setStroke(new BasicStroke(2F));
		g.draw(shape);
		g.setColor(Color.white);
		g.fill(shape);
		g.setTransform(tr);
	}

	private void addCategoryList(Graphics2D g, Font f, int size) {
		int ox = CALENDAR_SIZE+SCREENSHOT_WIDTH*2+4;
		int oy = 8;
		f = new Font(f.getName(), Font.BOLD, 15);
		g.setFont(f);
		for (ActivityEntry ae : categories) {
			ae.isActive = activeCategories.contains(ae.category);
			int dy = oy+40*ae.index;
			Color c = new Color(ae.category.color);
			if (!ae.isActive) {
				c = c.darker().darker();
			}
			g.setColor(c);
			g.fillRect(ox+1, dy+1, 18, 18);
			g.setColor(new Color(ae.isActive ? 0 : 0x8a8a8a));
			g.drawRect(ox+1, dy+1, 18, 18);
			g.drawString(ae.category.name, ox+24, dy+16);
		}
		g.setColor(new Color(0));
	}

	private void addDescriptionText(Graphics2D g, Font f) {
		ArrayList<String> temp = new ArrayList();
		ArrayList<EmbeddedEvent> li = new ArrayList(currentItems);
		Collections.sort(li, embedByCategory);
		for (int i = 0; i < li.size(); i++) {
			EmbeddedEvent e = li.get(i);
			e.event.generateDescriptionText(temp);
			if (i < li.size()-1)
				temp.add("");
		}
		FontMetrics fm = g.getFontMetrics();
		ArrayList<String> desc = new ArrayList();
		int to = 8;
		int tw = CALENDAR_SIZE-to*2-12;
		for (String s : temp) {
			this.addOrSplitString(fm, tw, s, desc, s.startsWith("\t"));
		}
		f = new Font(f.getName(), Font.PLAIN, 13);
		g.setFont(f);
		int ox = to;
		int oy = CALENDAR_SIZE+16;
		for (int i = 0; i < desc.size(); i++) {
			String s = desc.get(i);
			int dx = ox;
			if (s.startsWith("\t")) {
				dx += f.getSize()*2;
			}
			int dy = oy+i*18;
			g.drawString(s, dx, dy);
		}
	}

	private void addOrSplitString(FontMetrics fm, int tw, String s, ArrayList<String> desc, boolean indent) {
		s = s.replace("\t", "");
		String pre = indent ? "\t" : "";
		int sw = fm.stringWidth(s);
		if (sw < tw) {
			desc.add(pre+s);
		}
		else {
			int idx = s.lastIndexOf(' ');
			String before = s.substring(0, idx);
			while (idx > 0 && fm.stringWidth(before) >= tw) {
				before = s.substring(0, idx);
				idx = before.lastIndexOf(' ');
			}
			before = s.substring(0, idx);
			desc.add(pre+before);
			this.addOrSplitString(fm, tw, s.substring(idx+1), desc, indent);
		}
	}

	private void addCalendarLabels(Graphics2D g, Font f, int size) {
		int ox = CALENDAR_SIZE/2;
		int oy = CALENDAR_SIZE/2;
		FontMetrics fm = g.getFontMetrics();
		f = new Font(f.getName(), Font.BOLD, size);
		g.setFont(f);
		List<Integer> years = renderer.getYears();
		AffineTransform tr = g.getTransform();
		for (int i = 0; i < years.size(); i++) {
			int year = years.get(i);
			double r = renderer.getArcCenterlineRadiusAt(i, 0);
			int x = ox;
			int y = (int)(oy-r*CALENDAR_SIZE/2D);
			x += 6;
			y += 5;
			g.drawString(String.valueOf(year), x, y);
		}
		f = new Font(f.getName(), Font.PLAIN, (int)(size*1.25));
		g.setFont(f);
		for (Month m : Month.values()) {
			int idx = m.ordinal();
			double a = 360/12D*(m.ordinal()+0.5);
			double ang = CalendarRenderer.getGuiAngle(a);
			//int r = CALENDAR_SIZE/2;
			double r = CALENDAR_SIZE/2D*(renderer.getArcCenterlineRadiusAt(years.size(), a)-0.025);
			int dx = ox+(int)(r*Math.cos(ang));
			int dy = oy-(int)(r*Math.sin(ang));
			String s = m.getDisplayName(TextStyle.FULL, Locale.getDefault());
			g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(a), dx, dy));
			dx -= fm.stringWidth(s)/2;
			g.drawString(s, dx, dy);
		}

		f = new Font(f.getName(), Font.BOLD, 16);
		g.setFont(f);
		g.setTransform(tr);
		String date = renderer.limitEnd.getFullName(false);
		g.setColor(new Color(0xffffff));
		int dw = fm.stringWidth(date);
		g.fillRect(ox-dw*3/4+4, oy-12, dw*3/2+8, 24);
		g.setColor(new Color(0));
		g.drawString(date, ox-dw/2-1, oy+6);
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
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GLFunctions.printGLErrors("Draw prepare A");
		renderedOutput.bind(false);
		GLFunctions.printGLErrors("FB bind");

		/*
		float f = 1-e.age/5F;
		if (f > 0 && e.holdover <= 0) {
			GL11.glColor4f(1, 0, 0, f);
			int d = 4;
			GLFunctions.drawQuadScreenCoords(x, y, d, SCREENSHOT_HEIGHT, VIDEO_WIDTH, VIDEO_HEIGHT);
			GLFunctions.drawQuadScreenCoords(x+SCREENSHOT_WIDTH-d, y, d, SCREENSHOT_HEIGHT, VIDEO_WIDTH, VIDEO_HEIGHT);
			GLFunctions.drawQuadScreenCoords(x, y, SCREENSHOT_WIDTH, d, VIDEO_WIDTH, VIDEO_HEIGHT);
			GLFunctions.drawQuadScreenCoords(x, y+SCREENSHOT_HEIGHT-d, SCREENSHOT_WIDTH, d, VIDEO_WIDTH, VIDEO_HEIGHT);
			GLFunctions.printGLErrors("Outline draw");
		}
		 */

		//f = 1-e.holdover/(float)FADEOUT_DAYS;
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GLFunctions.printGLErrors("Draw prepare B");
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
			if (pathToFFMPEG != null && ffmpegDataLine != null) {
				ffmpegDataLine.close();
				int code = process.waitFor();
				if (code != 0) {
					throw new RuntimeException("Process encountered error code: "+code);
				}
			}
			else {
				encoder.finish();
			}
			StatusHandler.postStatus("Video completion succeeded. Exported "+exportedFrames+" frames", 2500, false);
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
		activeCategories.clear();
		currentItems.clear();
		imageCache.clear();
		for (int i = 0; i < currentImages.length; i++) {
			currentImages[i] = null;
		}
		categories = null;
		isRendering = false;
		if (renderer != null)
			renderer.limitEnd = null;
		renderer = null;
		encoder = null;
		try {
			if (ffmpegDataLine != null)
				ffmpegDataLine.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		ffmpegDataLine = null;
		process = null;
		isInitialized = false;
		flipBuffers = false;
		exportedFrames = 0;
		if (renderedOutput != null)
			renderedOutput.clear();
		videoInsets.clear();
		RenderLoop.sendToDFX = true;
	}

	private HashSet<CalendarEvent> getCurrentItems() {
		ArrayList<File> ret = new ArrayList();
		Collection<GuiSection> cs = renderer.getSectionsAt(renderer.limitEnd);
		HashSet<CalendarEvent> li = new HashSet();
		for (GuiSection s : cs) {
			if (!s.section.isEmpty()) {
				li.addAll(s.getItems(true));/*
			Collection<GuiHighlight> c = renderer.getHighlightsInSection(s);
			for (GuiHighlight h : c) {
				li.addAll(h.getItems(false));
			}*/
			}
		}
		//else {
		GuiHighlight h = GuiElement.HIGHLIGHTS.isChecked() ? renderer.getHighlightAtDate(renderer.limitEnd) : null;
		if (h != null) {
			li.addAll(h.getItems(true));
		}
		//}
		return li;
	}

	private List<String> getFFMPEGArgs(File f) {
		//List<String> parts = new ArrayList(Arrays.asList(("-f rawvideo -pix_fmt 0rgb -s:v "+VIDEO_WIDTH+"x"+VIDEO_HEIGHT+" -r "+VIDEO_FPS+" -i tcp://localhost:"+PORT_NUMBER+"?listen -vcodec libx264 -vf eq=gamma="+String.valueOf(GAMMA)).split(" ")));
		List<String> parts = new ArrayList(Arrays.asList(("-f rawvideo -pix_fmt 0rgb -s:v "+VIDEO_WIDTH+"x"+VIDEO_HEIGHT+" -r "+VIDEO_FPS+" -i pipe: -c:v "+videoFormat.ffmpegArgs+" -vf eq=gamma="+String.valueOf(GAMMA)).split(" ")));
		parts.add(f.getAbsolutePath());
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
		private int holdover = 0;

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

	private static class ActivityEntry {

		private final ActivityCategory category;
		private final int index;

		private boolean isActive;

		private ActivityEntry(ActivityCategory a, int i) {
			category = a;
			index = i;
		}

	}

}
