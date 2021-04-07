package net.eagtek.metaballs.client.renderer;

import org.joml.Vector3f;

import net.eagtek.metaballs.client.GameClient;

public class LightData {
	
	public static enum LightType {
		DIRECTION, POINT, SPOT
	}
	
	public LightType type;

	public float lightR = 1.0f;
	public float lightG = 1.0f;
	public float lightB = 1.0f;

	public double lightX;
	public double lightY;
	public double lightZ;

	public float emission;
	public float pointsize;
	
	public Vector3f direction;
	public float spotRadius;
	
	public float drawPoint;
	public float lensFlare;
	
	public LightData(LightType type, float emission, float pointsize, double lightX, double lightY, double lightZ, float drawPoint, float lensFlare) {
		this.type = type;
		this.emission = emission;
		this.pointsize = pointsize;
		this.lightX = lightX;
		this.lightY = lightY;
		this.lightZ = lightZ;
		this.direction = new Vector3f();
		this.drawPoint = drawPoint;
		this.lensFlare = lensFlare;
	}
	
	public LightData setRGB(float r, float g, float b) {
		lightR = r;
		lightG = g;
		lightB = b;
		return this;
	}
	
	public LightData setTemperature(int kelvin) {
		ColorTemperature temp = GameClient.instance.getGlobalRenderer().colorTemperatures;
		lightR = temp.getLinearR(kelvin);
		lightG = temp.getLinearG(kelvin);
		lightB = temp.getLinearB(kelvin);
		return this;
	}
	
	public LightData setDirection(float x, float y, float z) {
		direction.set(x, y, z).normalize();
		return this;
	}
	
	public LightData setSpotRadius(float s) {
		spotRadius = s;
		return this;
	}
}
