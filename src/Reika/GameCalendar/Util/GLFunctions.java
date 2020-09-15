package Reika.GameCalendar.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javafx.scene.image.Image;

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

	public static synchronized ByteBuffer createDirectByteBuffer(int bytes) {
		return ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
	}

	public static IntBuffer createDirectIntBuffer(int ints) {
		return createDirectByteBuffer(ints << 2).asIntBuffer();
	}

	public static enum BlendMode {
		DEFAULT(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA),
		ALPHA(GL11.GL_ONE, GL11.GL_SRC_ALPHA),
		PREALPHA(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA),
		MULTIPLY(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA),
		ADDITIVE(GL11.GL_ONE, GL11.GL_ONE),
		ADDITIVEDARK(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR),
		OVERLAYDARK(GL11.GL_SRC_COLOR, GL11.GL_ONE),
		ADDITIVE2(GL11.GL_SRC_ALPHA, GL11.GL_ONE),
		INVERTEDADD(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);

		public final int sfactor;
		public final int dfactor;

		private BlendMode(int s, int d) {
			sfactor = s;
			dfactor = d;
		}

		public void apply() {
			GL11.glBlendFunc(sfactor, dfactor);
		}

		public boolean isColorBlending() {
			return this == ADDITIVE || this == ADDITIVE2 || this == ADDITIVEDARK;
		}
	}

	public static void renderJFXImage(Image img, int width, int height) {
		?
	}
}