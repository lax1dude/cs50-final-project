
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main() {
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

uniform mat4 matrix_p_inv;

vec3 getPosition(sampler2D dt, vec2 coord) {
	float depth = texture(dt, coord).r;
	vec4 tran = matrix_p_inv * vec4(coord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	return tran.xyz / tran.w;
}

in vec2 texCoord;

layout(location = 0) out vec3 fragOut;

uniform sampler2D positionTex;

void main() {
	vec3 posV = getPosition(positionTex, texCoord);
	if(posV.x == 0.00 && posV.y == 0.0 && posV.z == 0.0) discard;
	fragOut = posV;
}

#endif
