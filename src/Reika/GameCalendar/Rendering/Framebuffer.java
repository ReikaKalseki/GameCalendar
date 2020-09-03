package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import Reika.GameCalendar.Util.GLFunctions;

public class Framebuffer {

	public final int width;
	public final int height;

	private int bufferID = -1;
	private int textureID = -1;
	private int depthBuffer = -1;

	public Framebuffer(int w, int h) {
		this(w, h, false);
	}

	public Framebuffer(int w, int h, boolean msaa) {
		width = w;
		height = h;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		this.createFramebuffer(w, h, msaa);
		this.checkFramebufferComplete();
		GLFunctions.bindFramebuffer(0);
	}

	public void deleteFramebuffer() {
		this.unbindTexture();
		this.unbind();

		if (depthBuffer > -1) {
			GL30.glDeleteRenderbuffers(depthBuffer);
			depthBuffer = -1;
		}

		if (textureID > -1) {
			GL11.glDeleteTextures(textureID);
			textureID = -1;
		}

		if (bufferID > -1) {
			GLFunctions.bindFramebuffer(0);
			GL30.glDeleteFramebuffers(bufferID);
			bufferID = -1;
		}
	}

	private void createFramebuffer(int w, int h, boolean msaa) {
		bufferID = GL30.glGenFramebuffers();
		textureID = GL11.glGenTextures();
		depthBuffer = GL30.glGenRenderbuffers();

		if (msaa) {
			this.setFilterType(GL11.GL_LINEAR);
			GLFunctions.bindFramebuffer(bufferID);
			// create a multisampled color attachment texture
			GL32.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, textureID);
			GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, 4, GL32.GL_RGBA, width, height, true);
			GL32.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, 0);
			GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, textureID, 0);
			// create a (also multisampled) renderbuffer object for depth and stencil attachments
			GLFunctions.bindRenderbuffer(depthBuffer);
			GL32.glRenderbufferStorageMultisample(GL32.GL_RENDERBUFFER, 4, GL32.GL_DEPTH24_STENCIL8, width, height);
			GL32.glBindRenderbuffer(GL32.GL_RENDERBUFFER, 0);
			GL32.glFramebufferRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_STENCIL_ATTACHMENT, GL32.GL_RENDERBUFFER, depthBuffer);
		}
		else {
			this.setFilterType(GL11.GL_NEAREST);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
			GLFunctions.bindFramebuffer(bufferID);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureID, 0);

			GLFunctions.bindRenderbuffer(depthBuffer);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT24, w, h);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
		}

		this.clear();
		this.unbindTexture();
	}

	public void setFilterType(int type) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, type);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, type);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void checkFramebufferComplete() {
		int code = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

		switch(code) {
			case GL32.GL_FRAMEBUFFER_COMPLETE:
				return;
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			default:
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status value:" + code);
		}
	}

	private void bindTexture() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

	private void unbindTexture() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void bind(boolean redoView) {
		GLFunctions.bindFramebuffer(bufferID);

		if (redoView)
			GL11.glViewport(0, 0, width, height);
	}

	public void unbind() {
		GLFunctions.bindFramebuffer(0);
	}

	public void draw() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glColorMask(true, true, true, false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, width, height);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		this.bindTexture();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2d(-1, -1);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2d(-1, 1);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2d(1, 1);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2d(1, -1);
		GL11.glEnd();
		this.unbindTexture();
		GL11.glPopAttrib();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
	}

	public void clear() {
		this.bind(true);
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClearDepth(1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		this.unbind();
	}

	public void sendTo(Framebuffer other) {
		this.sendTo(other.bufferID);
	}

	public void sendTo(int otherBuffer) {
		GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, otherBuffer);
		GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, bufferID);
		GL32.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
	}

	public String saveAsFile(File f) {
		try {
			int k = width * height;

			IntBuffer pixelBuffer = BufferUtils.createIntBuffer(k);
			int[] pixelValues = new int[k];

			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
			GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);

			pixelBuffer.get(pixelValues);
			GLFunctions.flipPixelArray(pixelValues, width, height);
			BufferedImage bufferedimage = null;

			bufferedimage = new BufferedImage(width, height, 1);

			for (int i1 = 0; i1 < height; ++i1) {
				for (int j1 = 0; j1 < width; ++j1) {
					bufferedimage.setRGB(j1, i1, pixelValues[i1 * width + j1]);
				}
			}

			f.getParentFile().mkdirs();
			ImageIO.write(bufferedimage, "png", f);
			return f.getAbsolutePath();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}