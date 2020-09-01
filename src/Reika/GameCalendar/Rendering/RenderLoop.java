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
import Reika.GameCalendar.GUI.JFXWindow;

public class RenderLoop extends Thread {

	private DriftFXSurface surface;
	private Swapchain chain;
	private Renderer hook;

	private int width;
	private int height;

	private TransferType transfer = StandardTransferTypes.NVDXInterop;
	private long contextID;
	private GLCapabilities glCaps;

	private boolean shouldClose = false;

	private int depthTex;
	private int dfxFramebuffer;

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
			GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 4);
			GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
			contextID = GLFW.glfwCreateWindow(800, 800, GLFWWindow.PROGRAM_TITLE, 0, 0);
			if (contextID == 0) {
				throw new RuntimeException("Failed to create window");
			}
			GLFW.glfwMakeContextCurrent(contextID);
			glCaps = GL.createCapabilities();
			GLFW.glfwShowWindow(contextID);
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
				this.renderLoop();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				shouldClose = true;
			}
		}
	}

	private void renderLoop() throws InterruptedException {
		if (surface == null) {
			this.load();
			return;
		}
		Vec2i size = hook.getSize();

		if (size.x == 0 || size.y == 0)
			throw new RuntimeException("Render box is size zero!");

		if (chain == null || size.x != width || size.y != height) {
			System.err.println("(re)create swapchain");
			if (chain != null) {
				chain.dispose();
			}

			chain = hook.createSwapchain(new SwapchainConfig(size, 2, PresentationMode.MAILBOX, transfer));

			width = size.x;
			height = size.y;
		}

		RenderTarget target = chain.acquire();

		this.bindToDFX(target);

		this.render(width, height);

		this.unbindDFX();

		chain.present(target);
		//Thread.sleep(16);
	}

	private void bindToDFX(RenderTarget target) {
		int tex = GLRenderer.getGLTextureId(target);
		depthTex = GL11.glGenTextures();
		GL32.glBindTexture(GL11.GL_TEXTURE_2D, depthTex);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL32.GL_DEPTH_COMPONENT32F, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		dfxFramebuffer = GL32.glGenFramebuffers();

		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, dfxFramebuffer);
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
	}

	private void unbindDFX() {
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
		GL32.glDeleteFramebuffers(dfxFramebuffer);
		GL11.glDeleteTextures(depthTex);
	}

	private void render(int x, int y) throws InterruptedException {
		long pre = System.currentTimeMillis();
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glViewport(0, 0, x, y);
		GLFW.glfwSwapBuffers(contextID);
		GLFW.glfwPollEvents();
		Main.getCalendarRenderer().draw(x, y);
		long post = System.currentTimeMillis();
		long sleep = GLFWWindow.MILLIS_PER_FRAME-(post-pre);
		if (sleep > 0) {
			Thread.sleep(sleep);
		}
	}

	public void close() {
		chain.dispose();
		chain = null;
		GLFW.glfwDestroyWindow(contextID);
		GLFW.glfwTerminate();
	}

}
