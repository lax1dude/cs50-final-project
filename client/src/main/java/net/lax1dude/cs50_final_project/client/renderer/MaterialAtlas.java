package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglFramebuffer;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglFramebuffer.DepthBufferType;

public class MaterialAtlas {
	
	public final EaglFramebuffer glObject;
	public final int size;
	
	public MaterialAtlas(GlobalRenderer renderer, int size, int tileSize) {
		this.glObject = new EaglFramebuffer(DepthBufferType.NONE, GL_RGBA8);
		
		this.size = size;
		
	}
	
}
