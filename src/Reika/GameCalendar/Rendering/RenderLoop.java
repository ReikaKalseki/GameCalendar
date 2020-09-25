package Reika.GameCalendar.Rendering;

import java.nio.ByteBuffer;

import org.eclipse.fx.drift.DriftFXSurface;
import org.eclipse.fx.drift.GLRenderer;
import org.eclipse.fx.drift.PresentationMode;
import org.eclipse.fx.drift.RenderTarget;
import org.eclipse.fx.drift.Renderer;
import org.eclipse.fx.drift.StandardTransferTypes;
import org.eclipse.fx.drift.Swapchain;
import org.eclipse.fx.drift.SwapchainConfig;
import org.eclipse.fx.drift.TransferType;
import org.eclipse.fx.drift.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLCapabilities;

import Reika.GameCalendar.Main;
import Reika.GameCalendar.GUI.GLFWInputHandler;
import Reika.GameCalendar.GUI.JFXWindow;
import Reika.GameCalendar.GUI.StatusHandler;
import Reika.GameCalendar.Util.GLFunctions;
import Reika.GameCalendar.Util.MovingAverage;
import Reika.GameCalendar.VideoExport.VideoRenderer;

public class RenderLoop extends Thread {

	public static boolean enableFPS = false;
	public static boolean sendToDFX = true;

	private DriftFXSurface surface;
	private Swapchain chain;
	private Renderer hook;

	private int width;
	private int height;

	private GLFWInputHandler input;
	private TransferType transfer = StandardTransferTypes.NVDXInterop;
	private long contextID = -1;
	private GLCapabilities glCaps;

	private boolean shouldClose = false;
	private final MovingAverage FPS = new MovingAverage(30);

	private Framebuffer msaaBuffer;
	private Framebuffer intermediate;

	public RenderLoop() {

	}

	private void load() {
		if (this.loadFrom(JFXWindow.getRenderPane())) {
			//ctx = org.eclipse.fx.drift.internal.GL.createContext(0, 1, 0);
			/*
			contextID = org.eclipse.fx.drift.internal.GL.createSharedCompatContext(0);
			org.eclipse.fx.drift.internal.GL.makeContextCurrent(contextID);
			glCaps = GL.createCapabilities();
			 */
			GLFW.glfwInit();
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
			//GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 4);
			//GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
			contextID = GLFW.glfwCreateWindow(800, 800, GLFWWindow.PROGRAM_TITLE, 0, 0);
			if (contextID == 0) {
				throw new RuntimeException("Failed to create window");
			}
			GLFW.glfwHideWindow(contextID);
			GLFW.glfwMakeContextCurrent(contextID);
			glCaps = GL.createCapabilities();
			StatusHandler.postStatus("Renderer initialized.", 10000);
		}
		else {
			//not feasible StatusHandler.postStatus("Renderer waiting for DFX surface...", 10000);
		}
	}

	private boolean loadFrom(DriftFXSurface surf) {
		if (surf == null)
			return false;
		surface = surf;
		hook = GLRenderer.getRenderer(surface);
		return true;
	}

	@Override
	public void run() {
		while (!shouldClose) {
			try {
				long pre = System.currentTimeMillis();
				this.renderLoop();
				long post = System.currentTimeMillis();
				if (enableFPS) {
					long dur = Math.max(1, post-pre);
					FPS.addValue(1000/dur);
				}
				if (!VideoRenderer.instance.isRendering()) {
					long sleep = GLFWWindow.MILLIS_PER_FRAME-(post-pre);
					if (sleep > 0) {
						Thread.sleep(sleep);
					}
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				shouldClose = true;
			}
		}
		if (chain != null)
			chain.dispose();
		chain = null;
		GLFW.glfwDestroyWindow(contextID);
		GLFW.glfwTerminate();
	}

	private void renderLoop() throws InterruptedException {
		if (surface == null) {
			this.load();
			return;
		}
		Vec2i size = hook.getSize();

		if (size.x == 0 || size.y == 0) {
			shouldClose = true;
			throw new RuntimeException("Render box is size zero!");
		}

		if (chain == null || size.x != width || size.y != height) {
			System.out.println("Recreating swapchain");
			if (chain != null) {
				chain.dispose();
			}

			chain = hook.createSwapchain(new SwapchainConfig(size, 2, PresentationMode.MAILBOX, transfer));

			width = size.x;
			height = size.y;

			//input = new GLFWInputHandler(width, height, contextID);
			//GLFW.glfwSetCursorPosCallback(contextID, input.mouseCall);
			//GLFW.glfwSetMouseButtonCallback(contextID, input.clickCall);

			msaaBuffer = null;
			intermediate = null;
			StatusHandler.postStatus("DriftFX interface loaded", 750, false);
		}
		GLFunctions.printGLErrors("Main loop");
		if (contextID <= 0)
			return;

		if (msaaBuffer == null)
			msaaBuffer = new Framebuffer(width, height, true);

		if (intermediate == null)
			intermediate = new Framebuffer(width, height).setClear(1, 1, 1);

		GLFunctions.printGLErrors("Pre-render");
		msaaBuffer.bind(false);
		GLFunctions.printGLErrors("Framebuffer bind");
		this.render(width, height);
		GLFunctions.printGLErrors("Draw");
		msaaBuffer.unbind();
		GLFunctions.printGLErrors("Framebuffer unbind");
		msaaBuffer.sendTo(intermediate);
		GLFunctions.printGLErrors("Framebuffer copy to intermediate");

		GL11.glFlush();

		if (VideoRenderer.instance.isRendering()) {
			VideoRenderer.instance.addFrame(intermediate);
		}

		if (sendToDFX) {
			RenderTarget target = chain.acquire();

			int tex = GLRenderer.getGLTextureId(target);
			int depthTex = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTex);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL32.GL_DEPTH_COMPONENT32F, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			int fb = GL32.glGenFramebuffers();

			GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fb);
			GL32.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, tex, 0);
			GL32.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, depthTex, 0);

			int status = GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER);
			switch (status) {
				case GL32.GL_FRAMEBUFFER_COMPLETE:
					break;
				case GL32.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
					System.err.println("INCOMPLETE_ATTACHMENT!");
					break;
			}

			GLFunctions.printGLErrors("DFX Framebuffer bind");
			//intermediate.sendTo(fb);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			intermediate.draw();
			GLFunctions.printGLErrors("Framebuffer render to DFX");
			//this.render(width, height);

			GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
			GL32.glDeleteFramebuffers(fb);
			GL11.glDeleteTextures(depthTex);
			GLFunctions.printGLErrors("DFX Framebuffer unbind");

			chain.present(target);
		}
	}

	private void render(int x, int y) throws InterruptedException {
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(0, 0, x, y);
		Main.getCalendarRenderer().draw(x, y);
	}

	public void close() {
		shouldClose = true;
	}

	public long getFPS() {
		if (!enableFPS)
			throw new IllegalStateException("FPS is not enabled!");
		return Math.round(FPS.getAverage());
	}

}
