package Reika.GameCalendar.Rendering;

import org.eclipse.fx.drift.DriftFXSurface;
import org.eclipse.fx.drift.GLRenderer;
import org.eclipse.fx.drift.PresentationMode;
import org.eclipse.fx.drift.Renderer;
import org.eclipse.fx.drift.StandardTransferTypes;
import org.eclipse.fx.drift.Swapchain;
import org.eclipse.fx.drift.SwapchainConfig;
import org.eclipse.fx.drift.TransferType;
import org.eclipse.fx.drift.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
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
	private long contextID = -1;
	private GLCapabilities glCaps;

	private Framebuffer msaaBuffer;
	private Framebuffer intermediate;

	private boolean shouldClose = false;

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
			this.createWindow(800, 800);
		}
	}

	private boolean loadFrom(DriftFXSurface surf) {
		if (surf == null)
			return false;
		surface = surf;
		hook = GLRenderer.getRenderer(surface);
		return true;
	}

	private void createWindow(int width, int height) {
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		//GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 4);
		//GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
		if (contextID > 0)
			GLFW.glfwDestroyWindow(contextID);
		contextID = GLFW.glfwCreateWindow(width, height, GLFWWindow.PROGRAM_TITLE, 0, 0);
		System.out.println("Created window ID "+contextID);
		if (contextID == 0) {
			throw new RuntimeException("Failed to create window");
		}
		GLFW.glfwShowWindow(contextID);
		GLFW.glfwMakeContextCurrent(contextID);
		glCaps = GL.createCapabilities();
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
		if (chain != null)
			chain.dispose();
		chain = null;
		if (contextID > 0)
			GLFW.glfwDestroyWindow(contextID);
		GLFW.glfwTerminate();
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
			System.out.println("Recreating swapchain");
			if (chain != null) {
				chain.dispose();
			}

			chain = hook.createSwapchain(new SwapchainConfig(size, 2, PresentationMode.MAILBOX, transfer));

			width = size.x;
			height = size.y;

			msaaBuffer = null;

			this.createWindow(width, height);
		}

		if (contextID <= 0)
			return;

		if (msaaBuffer == null) {
			//this.setupMSAA();
			msaaBuffer = new Framebuffer(width, height, true);
		}

		if (intermediate == null) {
			intermediate = new Framebuffer(width, height);
		}

		/*
		int fb = GL32.glGenFramebuffers();

		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, msaaBuffer);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		this.render(width, height);
		// 2. now blit multisampled buffer(s) to normal colorbuffer of intermediate FBO. Image is stored in screenTexture
		GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, msaaBuffer);
		GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, fb);
		GL32.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
		 */

		/*
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
		}*/

		msaaBuffer.bind(false);
		this.render(width, height);
		msaaBuffer.unbind();
		msaaBuffer.sendTo(intermediate);
		int error = GL11.glGetError();
		while (error != GL11.GL_NO_ERROR) {
			System.out.println("GL Error: "+error);
			error = GL11.glGetError();
		}
		intermediate.draw();
		/*
		if (true) {
			File f = new File("E:/bufferimage.png");
			String s = intermediate.saveAsFile(f);
			System.out.println(s);
			shouldClose = true;
		}*/
		/*
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL32.glActiveTexture(GL32.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, ??);
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2d(-1, -1);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2d(-1, 1);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2d(1, 1);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2d(1, -1);
		GL11.glEnd();*/

		/*
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
		GL32.glDeleteFramebuffers(fb);
		GL11.glDeleteTextures(depthTex);

		chain.present(target);
		 */
		//Thread.sleep(16);

		GLFW.glfwSwapBuffers(contextID);
		GLFW.glfwPollEvents();
	}

	private void setupMSAA() {
		/*
		msaaBuffer = GL32.glGenFramebuffers();
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, msaaBuffer);
		// create a multisampled color attachment texture
		msaaColorBuffer = GL32.glGenTextures();
		GL32.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, msaaColorBuffer);
		GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, 4, GL32.GL_RGB, width, height, true);
		GL32.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, 0);
		GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, msaaColorBuffer, 0);
		// create a (also multisampled) renderbuffer object for depth and stencil attachments
		msaaRBO = GL32.glGenRenderbuffers();
		GL32.glBindRenderbuffer(GL32.GL_RENDERBUFFER, msaaRBO);
		GL32.glRenderbufferStorageMultisample(GL32.GL_RENDERBUFFER, 4, GL32.GL_DEPTH24_STENCIL8, width, height);
		GL32.glBindRenderbuffer(GL32.GL_RENDERBUFFER, 0);
		GL32.glFramebufferRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_STENCIL_ATTACHMENT, GL32.GL_RENDERBUFFER, msaaRBO);

		if (GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("MSAA Framebuffer is not complete!");
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
		 */
	}

	private void render(int x, int y) throws InterruptedException {
		long pre = System.currentTimeMillis();
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(0, 0, x, y);
		//GLFW.glfwSwapBuffers(contextID);
		//GLFW.glfwPollEvents();
		Main.getCalendarRenderer().draw(x, y);
		long post = System.currentTimeMillis();
		long sleep = GLFWWindow.MILLIS_PER_FRAME-(post-pre);
		if (sleep > 0) {
			Thread.sleep(sleep);
		}
	}

	public void close() {
		shouldClose = true;
	}

}
