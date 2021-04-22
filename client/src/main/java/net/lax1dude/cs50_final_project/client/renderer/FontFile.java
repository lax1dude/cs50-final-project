package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

import net.lax1dude.cs50_final_project.client.GameClient;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglContext;
import net.lax1dude.cs50_final_project.client.renderer.opengl.GLStateManager;

public class FontFile {
	
	public final int glTexture2D;
	public final int size;
	public final int levels;
	public final int imageAtlasGridSize;
	public final int[] glyphOffsets;
	public final byte[] glyphSizes;
	
	private boolean destroyed = false;

	public void destroy() {
		if(!destroyed && EaglContext.contextAvailable()) {
			glDeleteTextures(glTexture2D);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			glDeleteTextures(glTexture2D);
			GameClient.log.warn("Font file #"+this.glTexture2D+" leaked memory");
			destroyed = true;
		}
	}
	
	public FontFile(InputStream effFile) throws IOException {
		DataInputStream d = new DataInputStream(effFile);
		if(!(d.read() == 'E' && d.read() == 'F' && d.read() == 'F')) {
			throw new IOException("Not an EFF file");
		}
		
		this.glTexture2D = glGenTextures();
		
		GLStateManager.bindTexture2D(glTexture2D, 0);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);//GL_NEAREST_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		size = d.read();
		levels = d.read();
		int renderedChars = d.readInt();
		
		imageAtlasGridSize = (int)Math.ceil(Math.sqrt(renderedChars));
		
		glyphOffsets = new int[65536];
		for(int i = 0; i < 65536; ++i) {
			glyphOffsets[i] = (int)d.readShort() & 0xFFFF;
		}
		
		glyphSizes = new byte[renderedChars];
		d.read(glyphSizes);
		
		//for(int i = 0; i < levels; ++i) {
			int texSize = (int)d.readShort() & 0xFFFF;
			int bytesRemaining = d.readInt();
			
			ByteBuffer data = MemoryUtil.memAlloc(bytesRemaining);
			ByteBuffer upload = MemoryUtil.memAlloc(texSize * texSize);
			
			byte[] read = new byte[bytesRemaining];
			d.read(read);
			data.put(read);
			data.flip();
			
			int size = LZ4.LZ4_decompress_safe(data, upload);
			
			if(size <= 0) {
				throw new IOException("Could not decompress");
			}
			
			upload.position(0);
			upload.limit(texSize * texSize);
			
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, texSize, texSize, 0, GL_RED, GL_UNSIGNED_BYTE, upload);
			glGenerateMipmap(GL_TEXTURE_2D);
			
			MemoryUtil.memFree(data);
			MemoryUtil.memFree(upload);
		//}
	}
	
	public int getFontAtlasSize() {
		return this.imageAtlasGridSize * (size + 2);
	}
	
	public void bindTexture() {
		GLStateManager.bindTexture2D(glTexture2D, 0);
	}

}
