package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengles.GLES20.GL_FLOAT;
import static org.lwjgl.opengles.GLES20.GL_STATIC_DRAW;
import static org.lwjgl.opengles.GLES20.GL_TRIANGLES;
import static org.lwjgl.opengles.GLES20.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengles.GLES20.glBindBuffer;
import static org.lwjgl.opengles.GLES20.glBufferData;
import static org.lwjgl.opengles.GLES20.glEnableVertexAttribArray;
import static org.lwjgl.opengles.GLES20.glVertexAttribPointer;
import static org.lwjgl.opengles.GLES30.glDrawArraysInstanced;
import static org.lwjgl.opengles.GLES30.glVertexAttribDivisor;
import static org.lwjgl.opengles.GLES31.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.eagtek.eagl.GLStateManager;
import net.eagtek.metaballs.client.GameClient;

public class UIRenderer {
	
	public final GameClient client;

	private final int text_glVertexArray;
	private final int text_glVertexBuffer;
	private final int text_glInstancingBuffer;

	private static final int instanceBufferSize = 10000;
	
	private final ByteBuffer uploadBuffer = MemoryUtil.memAlloc(instanceBufferSize * (20));
	
	public UIRenderer(GameClient client) {
		this.client = client;
		this.text_glVertexArray = glGenVertexArrays();
		this.text_glVertexBuffer = glGenBuffers();
		this.text_glInstancingBuffer = glGenBuffers();
		
		try(MemoryStack s = MemoryStack.stackPush()) {
			FloatBuffer f = s.mallocFloat(2 * 6);
			f.put(new float[] {
					0.0f, 0.0f,
					1.0f, 0.0f,
					1.0f, 1.0f,
					0.0f, 1.0f,
					0.0f, 0.0f,
					1.0f, 1.0f
			});
			f.flip();
			glBindBuffer(GL_ARRAY_BUFFER, text_glVertexBuffer);
			glBufferData(GL_ARRAY_BUFFER, f, GL_STATIC_DRAW);
		}

		glBindBuffer(GL_ARRAY_BUFFER, text_glInstancingBuffer);
		glBufferData(GL_ARRAY_BUFFER, uploadBuffer, GL_DYNAMIC_DRAW);
		
		GLStateManager.bindVertexArray(text_glVertexArray);
		
		glBindBuffer(GL_ARRAY_BUFFER, text_glVertexBuffer);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, text_glInstancingBuffer);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 20, 0);
		glVertexAttribDivisor(1, 1);
		glEnableVertexAttribArray(2);
		glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, 20, 12);
		glVertexAttribDivisor(2, 1);
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(3, 2, GL_UNSIGNED_SHORT, true, 20, 16);
		glVertexAttribDivisor(3, 1);
	}
	
	public void textBegin() {
		uploadBuffer.clear();
		charactersDrawn = 0;
		lineSpacing = 0.0f;
		charSpacing = 0.4f;
	}
	
	public int charactersDrawn = 0;
	
	public float lineSpacing = 0.0f;
	public float charSpacing = 0.4f;
	
	public void textDraw() {
		if(charactersDrawn > 0) {
			uploadBuffer.flip();
			GLStateManager.bindVertexArray(text_glVertexArray);
			glBindBuffer(GL_ARRAY_BUFFER, text_glInstancingBuffer);
			uploadBuffer.limit(charactersDrawn * 20);
			glBufferSubData(GL_ARRAY_BUFFER, 0, uploadBuffer);
			glDrawArraysInstanced(GL_TRIANGLES, 0, 6, charactersDrawn);
			textBegin();
		}
	}
	
	public void text(String text, FontFile f, int pixelsTall, int colorRGB, float colorA) {
		text(text, 0, 0, f, pixelsTall, colorRGB, colorA);
	}
	
	public void character(char text, float x, float y, FontFile f, int pixelsTall, int colorRGB, float colorA) {
		uploadBuffer.putFloat(x);
		uploadBuffer.putFloat(y);
		uploadBuffer.putFloat(pixelsTall);
		uploadBuffer.put((byte)(colorRGB >> 16));
		uploadBuffer.put((byte)(colorRGB >> 8));
		uploadBuffer.put((byte)(colorRGB));
		uploadBuffer.put((byte)(int)(colorA * 255.0f));
		uploadBuffer.putShort((short)(int)(((float)(f.glyphOffsets[(int)text] % f.imageAtlasGridSize) / (float)f.imageAtlasGridSize) * 65535.0f));
		uploadBuffer.putShort((short)(int)(((float)(f.glyphOffsets[(int)text] / f.imageAtlasGridSize) / (float)f.imageAtlasGridSize) * 65535.0f));
		++charactersDrawn;
	}
	
	public void bindColorShader(FontFile f, Matrix4f mat) {
		f.bindTexture();
		GlobalRenderer g = client.getGlobalRenderer();
		g.progManager.text_color.use();
		g.progManager.text_color.matrix_mvp.setMatrix4f(mat);
		g.progManager.text_color_fontSizePixelsOverTextureDimensions.set1f((float)f.size / f.getFontAtlasSize());
	}
	
	public void text(String text, float x, float y, FontFile f, int pixelsTall, int colorRGB, float colorA) {
		float xOffset = x;
		float yOffset = y;
		for(int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if(c == '\r') continue;
			if(c == '\n') {
				xOffset = x;
				yOffset += (1.0f + lineSpacing) * pixelsTall;
			}else {
				character(c, xOffset, yOffset, f, pixelsTall, colorRGB, colorA);
				xOffset += (((int)f.glyphSizes[f.glyphOffsets[(int)c]] & 255) + charSpacing) * ((float)pixelsTall / f.size);
				if(instanceBufferSize == charactersDrawn) {
					textDraw();
				}
			}
		}
	}
	
	public void text(String text, float x, float y, FontFile f, int pixelsTall, int colorRGB) {
		text(text, x, y, f, pixelsTall, colorRGB, 1.0f);
	}
	
	public void text(String text, FontFile f, int pixelsTall,int colorRGB) {
		text(text, f, pixelsTall, colorRGB, 1.0f);
	}
	
	public static final int floatToIntColor(float r, float g, float b) {
		return ((int)(r * 255.0f) << 16) | ((int)(g * 255.0f) << 8) | ((int)(b * 255.0f));
	}
	
	public void destroy() {
		glDeleteVertexArrays(this.text_glVertexArray);
		glDeleteBuffers(this.text_glVertexBuffer);
		glDeleteBuffers(this.text_glInstancingBuffer);
	}
	
}
