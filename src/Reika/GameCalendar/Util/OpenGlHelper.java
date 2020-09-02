package Reika.GameCalendar.Util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class OpenGlHelper {

	public static void bindFramebuffer(int id) {
		GL30.glBindFramebuffer(GL32.GL_FRAMEBUFFER, id);
	}

	public static void bindRenderbuffer(int id) {
		GL30.glBindRenderbuffer(GL32.GL_RENDERBUFFER, id);
	}

	public static void deleteRenderbuffer(int id)
	{
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
		GL30.glRenderbufferStorage(GL32.GL_RENDERBUFFER, GL32.GL_DEPTH_COMPONENT24, w, h);
	}

	public static void func_153190_b(int p_153190_3_)
	{
		GL30.glFramebufferRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, GL32.GL_RENDERBUFFER, p_153190_3_);
	}

	public static int verifyActiveFramebuffer() {
		return GL30.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER);
	}

	public static void setupFramebuffer(int id) {
		GL30.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, id, 0);
	}
}