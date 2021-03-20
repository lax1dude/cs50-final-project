package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

public class GLStateManager {

	private static int boundTexture2D = -1;
	
	public static final void bindTexture2D(int tex) {
		if(boundTexture2D != tex) glBindTexture(GL_TEXTURE_2D, boundTexture2D = tex);
	}
	
	private static int boundProgram = -1;
	
	public static final void bindProgram(int prog) {
		if(boundProgram != prog) glUseProgram(boundProgram = prog);
	}
}
