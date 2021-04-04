package net.eagtek.metaballs.client.renderer;

import java.util.LinkedHashSet;

import org.joml.Vector3f;

public class RenderScene {
	
	public final LinkedHashSet<ObjectRenderer> objectRenderers = new LinkedHashSet();
	public final LinkedHashSet<LightData> lightRenderers = new LinkedHashSet();
	public final LinkedHashSet<ShadowLightRenderer> shadowLightRenderers = new LinkedHashSet();
	public final LinkedHashSet<ParticleField> particleRenderers = new LinkedHashSet();
	public final LinkedHashSet<WaterRenderer> waterRenderers = new LinkedHashSet();
	
	public boolean enableSun = true;
	public Vector3f sunDirection = new Vector3f();

	public int sunKelvin = 3500;
	public int fogKelvin = 6000;
	
	public float sunBrightness = 100.0f;
	public float skyBrightness = 1.0f;
	public float sunSize = 0.1f;
	public float fogDensity = 0.005f;
	public boolean lightShafts = true;
	public float cloudDensity = 0.15f;

	public float windX = 0.01f;
	public float windZ = 0.004f;
	
	public RenderScene() {
		
	}

}
