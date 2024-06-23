#version 150

uniform sampler2D Sampler0;

uniform int DistortionType;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
	vec2 alteredTexCoord = texCoord0;

	if (DistortionType == 0) {
		// NORMAL

	} else if (DistortionType == 1) {
		// SWIRL
		vec2 center = vec2(0.5, 0.5);
		vec2 tc = alteredTexCoord - center;
		float dist = length(tc);
		float angle = atan(tc.y, tc.x);
		float radius = sqrt(tc.x * tc.x + tc.y * tc.y);
		angle += 4.0 * dist;
		tc = radius * vec2(cos(angle), sin(angle));
		alteredTexCoord = tc + center;

	} else if (DistortionType == 2) {
		// VERTICAL
		alteredTexCoord.y += sin(texCoord0.y * 10) * 0.09;

	} else if (DistortionType == 3) {
		// WIGGLE
		alteredTexCoord.x += sin(texCoord0.y * 50) * 0.015;
		alteredTexCoord.y += sin(texCoord0.x * 50) * 0.015;
	}

	fragColor = texture(Sampler0, alteredTexCoord);

}