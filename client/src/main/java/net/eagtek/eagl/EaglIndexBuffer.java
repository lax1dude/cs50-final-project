package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

import java.nio.ByteBuffer;

public class EaglIndexBuffer {

	public final int glObject;
	public final GLDataType indexType;
	private int bufferSize;
	private boolean destroyed = false;
	
	public EaglIndexBuffer(GLDataType indexType) {
		this.glObject = glGenBuffers();
		this.indexType = indexType;
		this.bufferSize = -1;
	}
	
	public int getIndiciesCount() {
		return bufferSize == -1 ? 0 : bufferSize / indexType.bytesUsed;
	}
	
	public EaglIndexBuffer upload(ByteBuffer data, boolean once) {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glObject);
		if(data.remaining() != bufferSize) {
			bufferSize = data.remaining();
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, once ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW);
		}else {
			glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, data);
		}
		return this;
	}
	
	public EaglIndexBuffer upload(ByteBuffer data) {
		return upload(data, true);
	}
	
	public EaglIndexBuffer uploadSub(int byteoffset, ByteBuffer data) {
		if(data.remaining() + byteoffset > bufferSize) {
			throw new IllegalArgumentException("Not enough space is allocated for this upload");
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glObject);
		glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, byteoffset, data);
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
			EaglContext.log.warn("GL index buffer #{} leaked memory", glObject);
			glDeleteBuffers(glObject);
			destroyed = true;
		}
	}
	
}
