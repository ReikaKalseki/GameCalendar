package Reika.GameCalendar.Util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public class GLFunctions {

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

	public static void bindFramebuffer(int id) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
	}

	public static void bindRenderbuffer(int id) {
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
	}

	public static void printGLErrors(String section) {
		int error = GL11.glGetError();
		boolean flag = false;
		while (error != GL11.GL_NO_ERROR) {
			flag = true;
			System.out.println("GL Error in "+section+": "+error);
			error = GL11.glGetError();
		}
		if (flag)
			Thread.dumpStack();
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
	/*
	public static void renderJFXImage(Image img, int width, int height) {
		?
	}*/

	/** With the window from [0, to size, top left origin] */
	public static void drawTextureAsQuadScreenCoords(int tex, int x, int y, int w, int h, int screenWidth, int screenHeight) {
		double rx = x/(double)screenWidth;
		double ry = y/(double)screenHeight;
		double rw = w/(double)screenWidth;
		double rh = h/(double)screenHeight;
		drawTextureAsQuadGLCoords(tex, rx*2-1, 1-ry*2, rw*2, -rh*2);
	}

	/** With the window from [-1 to +1], bottom left origin */
	public static void drawTextureAsQuadGLCoords(int tex, double x, double y, double w, double h) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glBegin(GL11.GL_QUADS);
		int v = h < 0 ? 1 : 0;
		int dv = 1-v;
		GL11.glTexCoord2f(0, dv);
		GL11.glVertex2d(x, y);
		GL11.glTexCoord2f(0, v);
		GL11.glVertex2d(x, y+h);
		GL11.glTexCoord2f(1, v);
		GL11.glVertex2d(x+w, y+h);
		GL11.glTexCoord2f(1, dv);
		GL11.glVertex2d(x+w, y);
		GL11.glEnd();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public static void writeTextureToImage(BufferedImage img, int x, int y, int width, int height, int tex) {
		int len = width * height;
		IntBuffer pixelBuffer = BufferUtils.createIntBuffer(len);
		int[] pixelValues = new int[len];

		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);

		pixelBuffer.get(pixelValues);
		GLFunctions.flipPixelArray(pixelValues, width, height);
		for (int k = 0; k < height; k++) {
			for (int i = 0; i < width; i++) {
				img.setRGB(i+x, k+y, pixelValues[k*width+i]);
			}
		}
	}
}