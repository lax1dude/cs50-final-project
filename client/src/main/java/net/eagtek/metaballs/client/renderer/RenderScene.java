package net.eagtek.metaballs.client.renderer;

import java.util.LinkedHashSet;

import org.joml.Vector3f;

public class RenderScene {

	public final LinkedHashSet<TerrainRenderer> terrainRenderers = new LinkedHashSet();
	public final LinkedHashSet<ObjectRenderer> objectRenderers = new LinkedHashSet();
	public final LinkedHashSet<LightData> lightRenderers = new LinkedHashSet();
	public final LinkedHashSet<ShadowLightRenderer> shadowLightRenderers = new LinkedHashSet();
	public final LinkedHashSet<ParticleField> particleRenderers = new LinkedHashSet();
	public final LinkedHashSet<WaterRenderer> waterRenderers = new LinkedHashSet();
	
	public boolean enableSun = true;
	public Vector3f sunDirection = new Vector3f();
	public float sunR = 1.0f;
	public float sunG = 1.0f;
	public float sunB = 1.0f;
	
	public RenderScene setSunBlackbody(float kelvin) {
		return this;
	}
	
	public RenderScene() {
		
	}

}
