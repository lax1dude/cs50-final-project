package net.eagtek.eagl.tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

public class MaterialConverterHelper {
	
	public static void mipmapAndCompress(int wh, ByteBuffer bufferA, ByteBuffer bufferB, ByteBuffer bufferC, OutputStream output) throws IOException {
		
		ByteBuffer pageACompress = MemoryUtil.memAlloc(LZ4.LZ4_compressBound(bufferA.remaining()));
		ByteBuffer pageBCompress = MemoryUtil.memAlloc(LZ4.LZ4_compressBound(bufferB.remaining()));
		ByteBuffer pageCCompress = MemoryUtil.memAlloc(LZ4.LZ4_compressBound(bufferC.remaining()));

		int c1 = LZ4.LZ4_compress_default(bufferA, pageACompress);
		int c2 = LZ4.LZ4_compress_default(bufferB, pageBCompress);
		int c3 = LZ4.LZ4_compress_default(bufferC, pageCCompress);

		if(c1 <= 0) {
			throw new RuntimeException("Could not compress A");
		}
		if(c2 <= 0) {
			throw new RuntimeException("Could not compress B");
		}
		if(c3 <= 0) {
			throw new RuntimeException("Could not compress C");
		}
		
		DataOutputStream dos = new DataOutputStream(output);
		
		dos.write((byte)'e');
		dos.write((byte)'M');
		dos.write((byte)'T');
		dos.write((byte)'L');
		
		dos.writeInt(wh);
		
		dos.writeInt(c1);
		
		byte[] b = new byte[c1];
		pageACompress.get(b);
		dos.write(b);
		
		dos.writeInt(c2);
		
		b = new byte[c2];
		pageBCompress.get(b);
		dos.write(b);
		
		dos.writeInt(c3);
		
		b = new byte[c3];
		pageCCompress.get(b);
		dos.write(b);
		
		MemoryUtil.memFree(pageACompress);
		MemoryUtil.memFree(pageBCompress);
		MemoryUtil.memFree(pageCCompress);
		
	}

}
