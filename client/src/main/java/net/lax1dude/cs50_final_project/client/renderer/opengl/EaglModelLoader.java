package net.lax1dude.cs50_final_project.client.renderer.opengl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglVertexArray.VertexAttribPointer;

public class EaglModelLoader {
	
	public static EaglVertexArray loadModel(InputStream source) throws IOException {
		DataInputStream d = new DataInputStream(source);
		if(d.read() == 0xEE && d.read() == 0xEE) {
			int flags = d.read();
			boolean index = (flags & 1) == 1;
			boolean texture = (flags & 2) == 2;
			boolean normal = (flags & 4) == 4;
			boolean compress = (flags & 8) == 8;
			boolean index32 = (flags & 16) == 16;
			boolean newFormat = (flags & 32) == 32;
			
			d.readUTF();

			int vboEntries = d.readInt();
			int iboEntries = d.readInt();
			
			int componentLen = 12;
			if(texture) componentLen += 8;
			if(normal) componentLen += 4;

			float minX = 0.0f; float minY = 0.0f; float minZ = 0.0f;
			float maxX = 0.0f; float maxY = 0.0f; float maxZ = 0.0f;
			
			if(newFormat) {
				minX = d.readFloat();
				minY = d.readFloat();
				minZ = d.readFloat();
				maxX = d.readFloat();
				maxY = d.readFloat();
				maxZ = d.readFloat();
			}

			int decompressedSize = d.readInt();
			int compressedSize = d.readInt();
			
			byte[] readBuffer = new byte[compressedSize];
			d.read(readBuffer);
			
			ByteBuffer upload;
			if(compress) {
				ByteBuffer data = MemoryUtil.memAlloc(compressedSize);
				upload = MemoryUtil.memAlloc(decompressedSize);
				
				data.put(readBuffer);
				data.flip();
				
				int size = LZ4.LZ4_decompress_safe(data, upload);
				
				MemoryUtil.memFree(data);
				
				if(size <= 0) {
					MemoryUtil.memFree(upload);
					throw new IOException("could not decompress vertex data");
				}
				
				upload.rewind();
				upload.limit(size);
			}else {
				upload = MemoryUtil.memAlloc(compressedSize);
				upload.put(readBuffer);
				upload.flip();
			}
			
			ArrayList<VertexAttribPointer> list = new ArrayList();
			int slot = 0;
			list.add(EaglVertexArray.attrib(0, slot++, 3, GLDataType.FLOAT, false, componentLen, 0));
			int componentLen2 = 12;
			if(normal) {
				list.add(EaglVertexArray.attrib(0, slot++, 4, GLDataType.BYTE, true, componentLen, componentLen2));
				componentLen2 += 4;
			}
			if(texture) {
				list.add(EaglVertexArray.attrib(0, slot++, 2, GLDataType.FLOAT, false, componentLen, componentLen2));
			}
			
			EaglVertexArray vao = new EaglVertexArray(new EaglVertexBuffer[] { new EaglVertexBuffer() }, list.toArray(new VertexAttribPointer[0]), index ? new EaglIndexBuffer(index32 ? GLDataType.INT_U : GLDataType.SHORT_U) : null);
			
			int iboOffset = componentLen * vboEntries;
			upload.rewind(); upload.limit(iboOffset);
			
			vao.buffers[0].upload(upload, true);
			
			vao.vertexes = vboEntries;

			vao.minX = minX;
			vao.minY = minY;
			vao.minZ = minZ;
			vao.maxX = maxX;
			vao.maxY = maxY;
			vao.maxZ = maxZ;
			
			if(vao.indexBuffer != null) {
				upload.limit(upload.capacity()); upload.position(iboOffset);
				vao.indexBuffer.upload(upload, true);
				vao.vertexes = iboEntries;
			}
			
			MemoryUtil.memFree(upload);
			
			return vao;
			
		}
		return null;
	}

}
