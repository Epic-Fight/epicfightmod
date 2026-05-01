#version 150

in vec4 vertexColor;
in vec4 normal;

out vec4 fragColor;

void main() {
    fragColor = vertexColor;
}
