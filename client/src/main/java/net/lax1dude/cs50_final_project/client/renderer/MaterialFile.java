package net.lax1dude.cs50_final_project.client.renderer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

public class MaterialFile {
	
	private boolean destroyed = false;
	
	public void destroy() {
		if(!destroyed) {
			destroyed = true;
			MemoryUtil.memFree(diffuse);
			MemoryUtil.memFree(normal);
			MemoryUtil.memFree(material);
		}
	}
	
	public void finalize() {
		destroy();
	}
	
	public final int widthHeight;
	public final ByteBuffer diffuse;
	public final ByteBuffer normal;
	public final ByteBuffer material;

	private MaterialFile(int widthHeight, ByteBuffer diffuse, ByteBuffer normal, ByteBuffer material) {
		this.widthHeight = widthHeight;
		this.diffuse = diffuse;
		this.normal = normal;
		this.material = material;
	}
	
	public static MaterialFile consumeStream(InputStream s) {
		try {
			DataInputStream s2 = new DataInputStream(s);
			if(s2.read() != (byte)'e' || s2.read() != (byte)'M' || s2.read() != (byte)'T' || s2.read() != (byte)'L') {
				throw new IOException("Not a material file");
			}
			
			int widthHeight = s2.readInt();
			int ds = widthHeight * widthHeight * 4;
			
			int cs = s2.readInt();

			ByteBuffer cb = MemoryUtil.memAlloc(cs);
			ByteBuffer diffuse = MemoryUtil.memAlloc(ds);

			byte[] csu = new byte[cs];
			s2.read(csu);
			cb.put(csu);
			cb.flip();
			int c = LZ4.LZ4_decompress_safe(cb, diffuse);
			MemoryUtil.memFree(cb);
			
			if(c <= 0) {
				MemoryUtil.memFree(diffuse);
				throw new IOException("Could not decompress");
			}
			
			cs = s2.readInt();

			cb = MemoryUtil.memAlloc(cs);
			ByteBuffer normal = MemoryUtil.memAlloc(ds);
			
			csu = new byte[cs];
			s2.read(csu);
			cb.put(csu);
			cb.flip();
			c = LZ4.LZ4_decompress_safe(cb, normal);
			MemoryUtil.memFree(cb);
			
			if(c <= 0) {
				MemoryUtil.memFree(diffuse);
				MemoryUtil.memFree(normal);
				throw new IOException("Could not decompress");
			}
			
			cs = s2.readInt();

			cb = MemoryUtil.memAlloc(cs);
			ByteBuffer material = MemoryUtil.memAlloc(ds);
			
			csu = new byte[cs];
			s2.read(csu);
			cb.put(csu);
			cb.flip();
			c = LZ4.LZ4_decompress_safe(cb, material);
			MemoryUtil.memFree(cb);
			
			if(c <= 0) {
				MemoryUtil.memFree(diffuse);
				MemoryUtil.memFree(normal);
				MemoryUtil.memFree(material);
				throw new IOException("Could not decompress");
			}
			
			MaterialFile ret = new MaterialFile(widthHeight, diffuse, normal, material);
			s.close();
			return ret;
			
		} catch (IOException e) {
			e.printStackTrace();
			try {
				s.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}
