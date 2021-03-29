package net.eagtek.metaballs;

public class Util {

	public static void sleep(long l) {
		try {
			Thread.sleep(l);
		} catch (InterruptedException e) {
		}
	}
	
	/*
	private static final Vector4f[] vertexes = new Vector4f[8];
	
	static {
		for(int i = 0; i < vertexes.length; ++i) {
			vertexes[i] = new Vector4f();
		}
	}
	
	public static boolean testBBFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f modelMatrix) {

		vertexes[0].x = minX;
		vertexes[0].y = minY;
		vertexes[0].z = minZ;
		
		vertexes[1].x = minX;
		vertexes[1].y = minY;
		vertexes[1].z = maxZ;
		
		vertexes[2].x = maxX;
		vertexes[2].y = minY;
		vertexes[2].z = maxZ;
		
		vertexes[3].x = maxX;
		vertexes[3].y = minY;
		vertexes[3].z = minZ;
		
		vertexes[4].x = minX;
		vertexes[4].y = maxY;
		vertexes[4].z = minZ;
		
		vertexes[5].x = minX;
		vertexes[5].y = maxY;
		vertexes[5].z = maxZ;
		
		vertexes[6].x = maxX;
		vertexes[6].y = maxY;
		vertexes[6].z = maxZ;
		
		vertexes[7].x = maxX;
		vertexes[7].y = maxY;
		vertexes[7].z = minZ;

		modelMatrix.transform(vertexes[0]);
		modelMatrix.transform(vertexes[1]);
		modelMatrix.transform(vertexes[2]);
		modelMatrix.transform(vertexes[3]);
		modelMatrix.transform(vertexes[4]);
		modelMatrix.transform(vertexes[5]);
		modelMatrix.transform(vertexes[6]);
		modelMatrix.transform(vertexes[7]);
		
		return false;
	}
	*/
	
}
