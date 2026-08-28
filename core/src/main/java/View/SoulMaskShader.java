package View;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class SoulMaskShader {
    private SoulMaskShader() {
    }

    public static final String VERTEX =
        "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    v_color = a_color;\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";

    public static final String FRAGMENT =
        "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_mask;\n" +
            "uniform vec4 u_maskRect;\n" +
            "void main() {\n" +
            "    vec4 src = texture2D(u_texture, v_texCoords) * v_color;\n" +
            "    vec2 maskUV = (gl_FragCoord.xy - u_maskRect.xy) / u_maskRect.zw;\n" +
            "    vec4 m = texture2D(u_mask, maskUV);\n" +
            "    float inside = step(0.0, maskUV.x) * step(maskUV.x, 1.0) * step(0.0, maskUV.y) * step(maskUV.y, 1.0);\n" +
            "    float maskA = max(m.a, m.r) * inside;\n" +
            "    gl_FragColor = vec4(src.rgb, src.a * maskA);\n" +
            "}";

    public static ShaderProgram create() {
        ShaderProgram.pedantic = false;
        ShaderProgram shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("Soul mask shader compile failed:\n" + shader.getLog());
        }
        return shader;
    }
}
