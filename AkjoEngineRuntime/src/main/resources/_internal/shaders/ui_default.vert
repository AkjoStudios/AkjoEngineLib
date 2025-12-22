#version 330 core
layout (location=0) in vec2 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;

uniform mat4 uProjection;

out vec4 fColor;
out vec2 fTexCoords;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
}