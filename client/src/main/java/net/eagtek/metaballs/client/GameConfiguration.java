package net.eagtek.metaballs.client;

public class GameConfiguration {

	public static final String gameName = "Metaballs";
	public static final int major = 0;
	public static final int minor = 0;
	public static final int patch = 1;
	public static final String versionString = "a" + major + "." + minor + "." + patch;

	public static String glslVersion = "#version 300 es";

	public static String glslVert_FloatPrecision       = "highp";
	public static String glslVert_IntPrecision         = "highp";
	public static String glslVert_SamplerPrecision     = "lowp";
	public static String glslVert_CubeSamplerPrecision = "lowp";
	
	public static String glslFrag_FloatPrecision       = "lowp";
	public static String glslFrag_IntPrecision         = "mediump";
	public static String glslFrag_SamplerPrecision     = "lowp";
	public static String glslFrag_CubeSamplerPrecision = "lowp";

	public static int sunShadowMapResolution = 2048;
	public static float sunShadowDistance = 200.0f;
	
	public static float sunShadowLODADistance = 10.0f;
	public static float sunShadowLODBDistance = 50.0f;
	public static float sunShadowLODCDistance = 250.0f;
	public static float sunShadowLODDDistance = 1250.0f;
	
}
