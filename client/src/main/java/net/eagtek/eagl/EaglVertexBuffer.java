package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES31.*;

import java.nio.ByteBuffer;

public class EaglVertexBuffer {
	
	public final int glObject;
	private int bufferSize;
	private boolean destroyed = false;
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public EaglVertexBuffer() {
		this.glObject = glGenBuffers();
		this.bufferSize = -1;
	}
	
	public EaglVertexBuffer upload(ByteBuffer data, boolean once) {
		glBindBuffer(GL_ARRAY_BUFFER, glObject);
		if(data.remaining() != bufferSize) {
			bufferSize = data.remaining();
			glBufferData(GL_ARRAY_BUFFER, data, once ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW);
		}else {
			glBufferSubData(GL_ARRAY_BUFFER, 0, data);
		}
		return this;
	}
	
	public EaglVertexBuffer upload(ByteBuffer data) {
		return upload(data, true);
	}
	
	public EaglVertexBuffer uploadSub(int byteoffset, ByteBuffer data) {
		if(data.remaining() + byteoffset > bufferSize) {
			throw new IllegalArgumentException("Not enough space is allocated for this upload");
		}
		glBindBuffer(GL_ARRAY_BUFFER, glObject);
		glBufferSubData(GL_ARRAY_BUFFER, byteoffset, data);
		return this;
	}
	
	public void destroy() {
		if(!destroyed) {
			glDeleteBuffers(glObject);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.warn("GL vertex buffer #{} leaked memory", glObject);
			glDeleteBuffers(glObject);
			destroyed = true;
		}
	}
	
}
