
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;

out vec3 posV;

uniform mat4 matrix_mv;
uniform mat4 matrix_p;

void main() {
    posV = (matrix_mv * vec4(posIn, 1.0)).xyz;
	gl_Position = matrix_p * vec4(posV, 1.0);
}

#endif

#ifdef FRAG

in vec3 posV;

layout(location = 0) out vec3 fragOut;

void main() {
    fragOut = posV;
}

#endif
