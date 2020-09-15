/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GameCalendar.Util;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class TextureLoader {

	public static final TextureLoader instance = new TextureLoader();

	/** Stores the image data for the texture during loading. */
	private long totalBytesLoaded = 0;
	private int boundTexture;

	private TextureLoader() {

	}

	/** Copy the supplied image onto a newly-allocated OpenGL texture, returning the allocated texture ID */
	public int allocateAndSetupTexture(BufferedImage buf, boolean clampEdge) {
		int i = GL11.glGenTextures();
		this.loadImageOntoTexture(buf, i, clampEdge);
		return i;
	}

	public void loadImageOntoTexture(BufferedImage buf, int texID, boolean clampEdge) {
		this.setupTextureExt(buf, texID, clampEdge);
	}

	private void setupTextureExt(BufferedImage buf, int texID, boolean clampEdge) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		if (clampEdge) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		}
		else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}

		int j = buf.getWidth();
		int k = buf.getHeight();

		int[] aint = new int[j * k];
		buf.getRGB(0, 0, j, k, aint, 0, j);

		IntBuffer imageData = IntBuffer.allocate(aint.length);
		imageData.put(aint);
		totalBytesLoaded += aint.length*4;
		imageData.position(0).limit(aint.length);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, j, k, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, imageData);
		imageData.clear();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public long getTotalBytesLoaded() {
		return totalBytesLoaded;
	}
}
