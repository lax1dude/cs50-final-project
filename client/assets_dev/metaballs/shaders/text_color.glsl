
#ifdef VERT

layout(location = 0) in vec2 vertIn;
layout(location = 1) in vec3 charPosHeight;
layout(location = 2) in vec4 charColor;
layout(location = 3) in vec2 charTex;

out vec2 v_tex;
out vec4 v_color;

uniform float fontSizePixelsOverTextureDimensions;

uniform mat4 matrix_mvp;

void main() {
	v_tex = charTex + (vertIn.xy * fontSizePixelsOverTextureDimensions);
	v_color = charColor;
    gl_Position = matrix_mvp * vec4(charPosHeight.xy + vec2(vertIn * charPosHeight.z), 0.0, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_tex;
in vec4 v_color;

uniform sampler2D tex;

layout(location = 0) out vec4 color;

void main() {
	float v = texture(tex, v_tex).r;
    color = vec4(v * v_color.rgb * v_color.a, v * v_color.a);
}

#endif