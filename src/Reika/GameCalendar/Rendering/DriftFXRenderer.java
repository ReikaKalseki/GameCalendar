package Reika.GameCalendar.Rendering;

import org.eclipse.fx.drift.GLRenderer;
import org.eclipse.fx.drift.PresentationMode;
import org.eclipse.fx.drift.RenderTarget;
import org.eclipse.fx.drift.Renderer;
import org.eclipse.fx.drift.StandardTransferTypes;
import org.eclipse.fx.drift.Swapchain;
import org.eclipse.fx.drift.SwapchainConfig;
import org.eclipse.fx.drift.Vec2i;

import Reika.GameCalendar.GUI.Window;

public class DriftFXRenderer implements Runnable {

	private Swapchain swapchain;

	private boolean shouldClose = false;

	@Override
	public void run() {


		while (!shouldClose) {
			// you acquire the Renderer api by calling getRenderer on your surface
			Renderer renderer = GLRenderer.getRenderer(Window.getRenderPane());

			// on your render thread you do the following:

			// first you create your own opengl context & make it current

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
			try {
				RenderTarget target = swapchain.acquire(); // this call is blocking, if there is no RenderTarget available it will wait until one gets available

				int texId = GLRenderer.getGLTextureId(target);

				// now you setup a framebuffer with this texture and draw onto it

				// once you are finished with the frame you call present on the swapchain
				swapchain.present(target);
			}
			catch (Exception e) {
				shouldClose = true;
				e.printStackTrace();
			}
		}
	}

}
