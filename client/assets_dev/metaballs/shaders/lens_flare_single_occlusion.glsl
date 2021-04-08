
#ifdef VERT

layout(location = 0) in vec2 posIn;

uniform vec2 position;
uniform vec2 size;

void main() {
    gl_Position = vec4(posIn * size + position, 0.0, 1.0);
}

#endif

#ifdef FRAG

void main() {
}

#endif