package Reika.GameCalendar.Rendering;

import org.eclipse.fx.drift.GLRenderer;
import org.eclipse.fx.drift.PresentationMode;
import org.eclipse.fx.drift.RenderTarget;
import org.eclipse.fx.drift.Renderer;
import org.eclipse.fx.drift.StandardTransferTypes;
import org.eclipse.fx.drift.Swapchain;
import org.eclipse.fx.drift.SwapchainConfig;
import org.eclipse.fx.drift.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;

import Reika.GameCalendar.Data.Timeline;
import Reika.GameCalendar.GUI.Window;

public class DriftFXRenderer implements Runnable {

	//private Pbuffer pbuffer;
	//private static Drawable drawable;

	private long windowID;

	private Renderer renderer;
	private Swapchain swapchain;

	private final CalendarRenderer calendar;

	private int textureID;
	private boolean shouldClose = false;

	public DriftFXRenderer(Timeline t) {
		calendar = new CalendarRenderer(t);
	}

	@Override
	public void run() {
		this.create();

		while (!shouldClose && GLFW.glfwWindowShouldClose(windowID)) {
			try {
				this.renderLoop();
			}
			catch (Exception e) {
				shouldClose = true;
				GLFW.glfwDestroyWindow(windowID);
				e.printStackTrace();
			}
		}
	}

	private void create() {
		// you acquire the Renderer api by calling getRenderer on your surface
		renderer = GLRenderer.getRenderer(Window.getRenderPane());

		// on your render thread you do the following:

		// first you create your own opengl context & make it current
		try {
			GLFW.glfwInit();
			//GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
			windowID = GLFW.glfwCreateWindow(600, 600, "", 0, 0);
			GLFW.glfwMakeContextCurrent(windowID);
			GL.createCapabilities();
			GLFW.glfwShowWindow(windowID);
			/*
			Display.setFullscreen(false);
			//Display.setParent(??);
			Display.setResizable(true);
			Display.create();
			Display.setVSyncEnabled(true);
			 */
			/*
			pbuffer = new Pbuffer(1, 1, new PixelFormat(), null, null, new ContextAttribs().withDebug(true));
			pbuffer.makeCurrent();
			drawable = pbuffer;
			 */
			textureID = GL11.glGenTextures();
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	private void renderLoop() throws Exception {
		// in your render loop you manage your swapchain instance

		// you can fetch the current size of the surface by asking the renderer
		Vec2i size = renderer.getSize();

		if (swapchain == null || size.x != swapchain.getConfig().size.x || size.y != swapchain.getConfig().size.y) {
			// re-create the swapchain
			if (swapchain != null) {
				swapchain.dispose();
			}
			swapchain = renderer.createSwapchain(new SwapchainConfig(size, 2, PresentationMode.MAILBOX, StandardTransferTypes.MainMemory));
		}


		// to draw you acquire a RenderTarget from the swapchain
		RenderTarget target = swapchain.acquire(); // this call is blocking, if there is no RenderTarget available it will wait until one gets available

		// now you setup a framebuffer with this texture and draw onto it
		int texId = GLRenderer.getGLTextureId(target);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		calendar.draw(size.x, size.y);
		GLFW.glfwSwapBuffers(windowID);
		GL11.glFlush();

		// once you are finished with the frame you call present on the swapchain
		swapchain.present(target);
	}

}
