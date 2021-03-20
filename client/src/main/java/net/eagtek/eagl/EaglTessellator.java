package net.eagtek.eagl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.system.jemalloc.JEmalloc;

public class EaglTessellator {

	public final ByteBuffer vertexBuffer;
	public final ByteBuffer indexBuffer;

	private final int bytesPerVertex;
	private final int vertexLimit;
	private int vertexId;

	private boolean throwOnOverflow;
	
	private boolean destroyed = false;
	
	public EaglTessellator(int bytesPerVertex, int vertexLimit, int indexLimit) {
		this.bytesPerVertex = bytesPerVertex;
		this.vertexLimit = vertexLimit;
		throwOnOverflow = false;
		vertexBuffer = JEmalloc.je_malloc(bytesPerVertex * vertexLimit).order(ByteOrder.nativeOrder());
		indexBuffer = indexLimit > 0 ? JEmalloc.je_malloc((vertexLimit > 65536) ? (4 * indexLimit) : (2 * indexLimit)).order(ByteOrder.nativeOrder()) : null;
	}
	
	public final EaglTessellator reset() {
		vertexId = 0;
		vertexBuffer.clear();
		if(indexBuffer != null) indexBuffer.clear();
		return this;
	}
	
	public final EaglTessellator throwOnOverflow(boolean yes) {
		this.throwOnOverflow = yes;
		return this;
	}
	
	private static final void throwOverflow() {
		throw new ArrayIndexOutOfBoundsException("Tessellator vertex limit reached");
	}

	public final EaglTessellator put_float(float p1) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putFloat(p1);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec2f(float p1, float p2) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putFloat(p1);
			vertexBuffer.putFloat(p2);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec3f(float p1, float p2, float p3) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putFloat(p1);
			vertexBuffer.putFloat(p2);
			vertexBuffer.putFloat(p3);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec4f(float p1, float p2, float p3, float p4) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putFloat(p1);
			vertexBuffer.putFloat(p2);
			vertexBuffer.putFloat(p3);
			vertexBuffer.putFloat(p4);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}

	public final EaglTessellator put_int(int p1) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putInt(p1);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec2i(int p1, int p2) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putInt(p1);
			vertexBuffer.putInt(p2);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec3i(int p1, int p2, int p3) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putInt(p1);
			vertexBuffer.putInt(p2);
			vertexBuffer.putInt(p3);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec4i(int p1, int p2, int p3, int p4) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putInt(p1);
			vertexBuffer.putInt(p2);
			vertexBuffer.putInt(p3);
			vertexBuffer.putInt(p4);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec2s(short p1, short p2) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putShort(p1);
			vertexBuffer.putShort(p2);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	// https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
	
	public static final int toHalfFloat(float fval) {
		int fbits = Float.floatToIntBits(fval);
		int sign = fbits >>> 16 & 0x8000; // sign only
		int val = (fbits & 0x7fffffff) + 0x1000; // rounded value

		if (val >= 0x47800000) // might be or become NaN/Inf
		{ // avoid Inf due to rounding
			if ((fbits & 0x7fffffff) >= 0x47800000) { // is or must become NaN/Inf
				if (val < 0x7f800000) // was value but too large
					return sign | 0x7c00; // make it +/-Inf
				return sign | 0x7c00 | // remains +/-Inf or NaN
						(fbits & 0x007fffff) >>> 13; // keep NaN (and Inf) bits
			}
			return sign | 0x7bff; // unrounded not quite Inf
		}
		if (val >= 0x38800000) // remains normalized value
			return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
		if (val < 0x33000000) // too small for subnormal
			return sign; // becomes +/-0
		val = (fbits & 0x7fffffff) >>> 23; // tmp exp for subnormal calc
		return sign | ((fbits & 0x7fffff | 0x800000) // add subnormal bit
				+ (0x800000 >>> val - 102) // round depending on cut off
				>>> 126 - val); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
	}
	
	public final EaglTessellator put_vec4s(short p1, short p2, short p3, short p4) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putShort(p1);
			vertexBuffer.putShort(p2);
			vertexBuffer.putShort(p3);
			vertexBuffer.putShort(p4);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec2hf(float p1, float p2) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putShort((short)toHalfFloat(p1));
			vertexBuffer.putShort((short)toHalfFloat(p2));
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec4hf(float p1, float p2, float p3, float p4) {
		if(vertexId < vertexLimit) {
			vertexBuffer.putShort((short)toHalfFloat(p1));
			vertexBuffer.putShort((short)toHalfFloat(p2));
			vertexBuffer.putShort((short)toHalfFloat(p3));
			vertexBuffer.putShort((short)toHalfFloat(p4));
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}
	
	public final EaglTessellator put_vec4b(byte p1, byte p2, byte p3, byte p4) {
		if(vertexId < vertexLimit) {
			vertexBuffer.put(p1);
			vertexBuffer.put(p2);
			vertexBuffer.put(p3);
			vertexBuffer.put(p4);
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return this;
	}

	public final int endVertex() {
		if(vertexId < vertexLimit) {
			vertexBuffer.position((vertexId + 1) * bytesPerVertex);
			return vertexId++;
		}else if(throwOnOverflow) {
			throwOverflow();
		}
		return 0;
	}

	public final int vertexesRemaining() {
		return vertexId - vertexLimit;
	}
	
	public final int indexesRemaining() {
		if(vertexBuffer.capacity() > 65536) {
			return indexBuffer.remaining() / 4;
		}else {
			return indexBuffer.remaining() / 2;
		}
	}
	
	public final EaglTessellator addToIndex(int vertexId) {
		if(indexBuffer == null) throw new IllegalStateException("this tessellator does not have an index buffer");
		if(vertexBuffer.capacity() > 65536) {
			if(indexBuffer.remaining() >= 4) {
				indexBuffer.putInt(vertexId);
			}else {
				throw new ArrayIndexOutOfBoundsException("index buffer is full");
			}
		}else {
			if(indexBuffer.remaining() >= 2) {
				indexBuffer.putShort((short)vertexId);
			}else {
				throw new ArrayIndexOutOfBoundsException("index buffer is full");
			}
		}
		return this;
	}
	
	public final EaglTessellator draw(EaglVertexArray array, int drawMode) {
		
		EaglVertexBuffer b = array.buffers[0];
		
		vertexBuffer.flip();
		if(b.getBufferSize() < vertexBuffer.remaining()) {
			b.upload(vertexBuffer, false);
		}else {
			b.uploadSub(0, vertexBuffer);
		}
		
		EaglIndexBuffer ib = array.indexBuffer;
		
		if(indexBuffer != null && ib != null) {
			indexBuffer.flip();
			if(ib.getBufferSize() < indexBuffer.remaining()) {
				ib.upload(indexBuffer, false);
			}else {
				ib.uploadSub(0, indexBuffer);
			}
		}
		
		array.draw(drawMode, 0, vertexId);
		
		reset();
		
		return this;
	}
	
	public final EaglTessellator uploadVertexes(EaglVertexBuffer buf, boolean once) {
		if(vertexBuffer.limit() == vertexBuffer.capacity()) {
			vertexBuffer.flip();
		}
		buf.upload(vertexBuffer, once);
		return this;
	}
	
	public final EaglTessellator uploadIndexes(EaglIndexBuffer buf, boolean once) {
		if(indexBuffer != null) {
			if(indexBuffer.limit() == indexBuffer.capacity()) {
				indexBuffer.flip();
			}
			buf.upload(indexBuffer, once);
		}
		return this;
	}

	public void destroy() {
		if(!destroyed) {
			JEmalloc.je_free(vertexBuffer);
			if(indexBuffer != null) JEmalloc.je_free(indexBuffer);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed) {
			EaglContext.log.warn("Tessellator {} leaked memory", this.toString());
			JEmalloc.je_free(vertexBuffer);
			if(indexBuffer != null) JEmalloc.je_free(indexBuffer);
			destroyed = true;
		}
	}
	
	
}
