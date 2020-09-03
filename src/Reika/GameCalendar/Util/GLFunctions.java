package Reika.GameCalendar.Util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class GLFunctions {

	public static void bindFramebuffer(int id) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
	}

	public static void bindRenderbuffer(int id) {
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
	}

	public static void printGLErrors(String section) {
		int error = GL11.glGetError();
		while (error != GL11.GL_NO_ERROR) {
			System.out.println("GL Error in "+section+": "+error);
			Thread.dumpStack();
			error = GL11.glGetError();
		}
	}

	public static void flipPixelArray(int[] data, int width, int height) {
		int[] temp = new int[width];
		int k = height / 2;

		for (int l = 0; l < k; ++l) {
			System.arraycopy(data, l * width, temp, 0, width);
			System.arraycopy(data, (height - 1 - l) * width, data, l * width, width);
			System.arraycopy(temp, 0, data, (height - 1 - l) * width, width);
		}
	}
}