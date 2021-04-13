package net.eagtek.metaballs.client.renderer;

public class TextRenderer {
	
	public final int glTexture2D;
	public final byte[] glyphSizes;
	
	public TextRenderer(int tex, byte[] gs) {
		this.glTexture2D = tex;
		this.glyphSizes = gs;
	}

	//private float pixelSizeX;
	//private float pixelSizeY;
	
	public void setPixelSize(float x, float y) {
		
	}

}
