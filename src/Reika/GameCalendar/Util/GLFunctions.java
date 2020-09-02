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

	public static void deleteRenderbuffer(int id) {
		GL30.glDeleteRenderbuffers(id);
	}

	public static void deleteFramebuffer(int id) {
		GL30.glDeleteFramebuffers(id);
	}

	public static int createFramebuffer() {
		return GL30.glGenFramebuffers();
	}

	public static int createRenderBuffer() {
		return GL30.glGenRenderbuffers();
	}

	public static void setupRenderbuffer(int w, int h) {
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT24, w, h);
	}

	public static int verifyActiveFramebuffer() {
		return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
	}

	public static void setupFramebuffer(int id) {
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, id, 0);
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