package Reika.GameCalendar.Rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;

import Reika.GameCalendar.Util.TextureLoader;

public class Texture {

	private Class reference;
	private String path;
	private File file;

	private final boolean clampEdge;
	private final boolean smoothSampling;

	private int glID = -1;

	public Texture(File f, boolean clamp, boolean sample) {
		this(clamp, sample);
		file = f;
	}

	public Texture(Class root, String path, boolean clamp, boolean sample) {
		this(clamp, sample);
		reference = root;
		this.path = path;
	}

	private Texture(boolean clamp, boolean sample) {
		clampEdge = clamp;
		smoothSampling = sample;
	}

	private void load() {
		try(InputStream in = this.getInputStream()) {
			BufferedImage img = ImageIO.read(in);
			glID = TextureLoader.instance.allocateAndSetupTexture(img, clampEdge, smoothSampling);
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	private InputStream getInputStream() throws Exception {
		if (reference != null)
			return reference.getResourceAsStream(path);
		else if (file != null)
			return new FileInputStream(file);
		else
			throw new RuntimeException("No input data for texture!");
	}

	public void bind() {
		if (glID == -1) {
			this.load();
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, glID);
	}

}
