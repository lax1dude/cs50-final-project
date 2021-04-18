package net.lax1dude.cs50_final_project.client.renderer;

public class ColorTemperature {
	
	private final byte[] lut;
	
	public ColorTemperature(byte[] lut) {
		this.lut = lut;
	}
	
	public float getLinearR(int temperatureKelvin) {
		return (float) Math.pow(getSRGBR(temperatureKelvin), 2.2f);
	}
	
	public float getLinearG(int temperatureKelvin) {
		return (float) Math.pow(getSRGBG(temperatureKelvin), 2.2f);
	}
	
	public float getLinearB(int temperatureKelvin) {
		return (float) Math.pow(getSRGBB(temperatureKelvin), 2.2f);
	}
	
	public float getSRGBR(int temperatureKelvin) {
		if(temperatureKelvin < 1000) temperatureKelvin = 1000;
		if(temperatureKelvin > 40000) temperatureKelvin = 40000;
		int k = ((temperatureKelvin - 100) / 100) * 3;
		return (float)((int)lut[k] & 0xFF) / 255.0f;
	}
	
	public float getSRGBG(int temperatureKelvin) {
		if(temperatureKelvin < 1000) temperatureKelvin = 1000;
		if(temperatureKelvin > 40000) temperatureKelvin = 40000;
		int k = ((temperatureKelvin - 100) / 100) * 3 + 1;
		return (float)((int)lut[k] & 0xFF) / 255.0f;
	}
	
	public float getSRGBB(int temperatureKelvin) {
		if(temperatureKelvin < 1000) temperatureKelvin = 1000;
		if(temperatureKelvin > 40000) temperatureKelvin = 40000;
		int k = ((temperatureKelvin - 100) / 100) * 3 + 2;
		return (float)((int)lut[k] & 0xFF) / 255.0f;
	}
	
}
