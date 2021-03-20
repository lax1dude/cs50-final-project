package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

public class EaglVertexArray {
	
	public static class VertexAttribPointer {

		public final int bufferNumber;
		public final int indexNumber;
		public final int componentCount;
		public final GLDataType datatype;
		public final boolean normalize;
		public final int strideBytes;
		public final int startByte;
		
		protected VertexAttribPointer(int bufferNumber, int indexNumber, int componentCount, GLDataType datatype, boolean normalize, int strideBytes, int startByte) {
			this.bufferNumber = bufferNumber;
			this.indexNumber = indexNumber;
			this.componentCount = componentCount;
			this.datatype = datatype;
			this.normalize = normalize;
			this.strideBytes = strideBytes;
			this.startByte = startByte;
		}
		
	}
	
	public static VertexAttribPointer attrib(int bufferNumber, int indexNumber, int componentCount, GLDataType datatype, boolean normalize, int strideBytes, int startByte) {
		return new VertexAttribPointer(bufferNumber, indexNumber, componentCount, datatype, normalize, strideBytes, startByte);
	}

	public final EaglVertexBuffer[] buffers;
	public final VertexAttribPointer[] pointers;
	public final EaglIndexBuffer indexBuffer;
	private boolean destroyed = false;
	
	public final int glObject;
	
	public EaglVertexArray(EaglVertexBuffer[] buffersv, VertexAttribPointer[] pointersv, EaglIndexBuffer indexBufferv) {
		buffers = buffersv;
		pointers = pointersv;
		indexBuffer = indexBufferv;
		glObject = glGenVertexArrays();
		
		glBindVertexArray(glObject);
		
		for(int i = 0; i < pointersv.length; ++i) {
			VertexAttribPointer ptr = pointersv[i];
			glBindBuffer(GL_ARRAY_BUFFER, buffersv[ptr.bufferNumber].glObject);
			glVertexAttribPointer(ptr.indexNumber, ptr.componentCount, ptr.datatype.glEnum, ptr.normalize, ptr.strideBytes, ptr.startByte);
		}
		
		if(indexBuffer != null) {
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.glObject);
		}
	}
	
	public EaglVertexArray(EaglVertexBuffer[] buffersv, VertexAttribPointer[] pointersv) {
		this(buffersv, pointersv, null);
	}
	
	public static int enabledVertexAttribArrays = 0;
	
	public static void setEnabledAttribArrays(int num) {
		if(enabledVertexAttribArrays != num) {
			if(enabledVertexAttribArrays > num) {
				for(int i = num; i < enabledVertexAttribArrays; ++i) {
					glDisableVertexAttribArray(i);
				}
			}else {
				for(int i = enabledVertexAttribArrays; i < num; ++i) {
					glEnableVertexAttribArray(i);
				}
			}
			enabledVertexAttribArrays = num;
		}
	}
	
	public void draw(int drawMode, int start, int len) {
		
		setEnabledAttribArrays(pointers.length);
		
		if(indexBuffer != null) {
			glDrawElements(drawMode, len, indexBuffer.indexType.glEnum, start * indexBuffer.indexType.bytesUsed);
		}else {
			glDrawArrays(drawMode, start, len);
		}
		
	}
	
	
	public void drawAll(int drawMode) {

		setEnabledAttribArrays(pointers.length);
		
		if(indexBuffer != null) {
			glDrawElements(drawMode, indexBuffer.getIndiciesCount(), indexBuffer.indexType.glEnum, 0);
		}else {
			throw new IllegalArgumentException("drawAll is for VAOs with index buffers");
		}
		
	}
	
	public void drawInstanced(int drawMode, int start, int len, int instances) {

		setEnabledAttribArrays(pointers.length);
		
		if(indexBuffer != null) {
			glDrawElementsInstanced(drawMode, len, indexBuffer.indexType.glEnum, start * indexBuffer.indexType.bytesUsed, instances);
		}else {
			glDrawArraysInstanced(drawMode, start, len, instances);
		}
		
	}
	
	public void destroy() {
		if(!destroyed) {
			glDeleteVertexArrays(glObject);
			destroyed = true;
		}
	}
	
	public void destroyWithBuffers() {
		destroy();
		for(EaglVertexBuffer b : buffers) {
			b.destroy();
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.warn("GL vertex array #{} leaked memory", glObject);
			glDeleteVertexArrays(glObject);
			destroyed = true;
		}
	}
}
