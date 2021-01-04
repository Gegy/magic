package dev.gegy.magic.client.glyph.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.render.GlyphRenderData;
import dev.gegy.magic.client.glyph.render.GlyphTexture;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

public final class GlyphTextureShader implements AutoCloseable {
    private static final String UNIFORM_TEXEL_SIZE = "texel_size";
    private static final String UNIFORM_RENDER_SIZE = "render_size";
    private static final String UNIFORM_FORM_PROGRESS = "form_progress";
    private static final String UNIFORM_COLOR = "color";
    private static final String UNIFORM_FLAGS = "flags";
    private static final String UNIFORM_STROKE = "stroke";

    private static final int STROKE_ACTIVE_BIT = 1 << 15;
    private static final int HIGHLIGHT_NODES_BIT = 1 << 16;

    private final GlyphShaderProgram program;

    private final int uniformTexelSize;
    private final int uniformRenderSize;
    private final int uniformFormProgress;
    private final int uniformColor;
    private final int uniformFlags;
    private final int uniformStroke;

    private final FloatBuffer colorData = MemoryUtil.memAllocFloat(3);
    private final FloatBuffer strokeData = MemoryUtil.memAllocFloat(4);

    private GlyphTextureShader(
            GlyphShaderProgram program,
            int uniformTexelSize, int uniformRenderSize,
            int uniformFormProgress, int uniformColor,
            int uniformFlags, int uniformStroke
    ) {
        this.program = program;
        this.uniformTexelSize = uniformTexelSize;
        this.uniformRenderSize = uniformRenderSize;
        this.uniformFormProgress = uniformFormProgress;
        this.uniformColor = uniformColor;
        this.uniformFlags = uniformFlags;
        this.uniformStroke = uniformStroke;
    }

    public static GlyphTextureShader create(ResourceManager resources) throws IOException {
        GlyphShaderProgram program = GlyphShaderProgram.compile(resources, Magic.identifier("glyph_texture"));

        int uniformTexelSize = program.getUniformLocation(UNIFORM_TEXEL_SIZE);
        int uniformRenderSize = program.getUniformLocation(UNIFORM_RENDER_SIZE);
        int uniformFormProgress = program.getUniformLocation(UNIFORM_FORM_PROGRESS);
        int uniformColor = program.getUniformLocation(UNIFORM_COLOR);
        int uniformFlags = program.getUniformLocation(UNIFORM_FLAGS);
        int uniformStroke = program.getUniformLocation(UNIFORM_STROKE);

        return new GlyphTextureShader(
                program,
                uniformTexelSize, uniformRenderSize,
                uniformFormProgress, uniformColor,
                uniformFlags, uniformStroke
        );
    }

    public void bind(GlyphTexture texture, GlyphRenderData renderData, float tickDelta) {
        GlProgramManager.useProgram(this.program.getProgramRef());

        GL20.glUniform1f(this.uniformTexelSize, texture.getTexelSize());
        GL20.glUniform1f(this.uniformRenderSize, texture.getRenderSize());

        GL20.glUniform1f(this.uniformFormProgress, renderData.formProgress);

        FloatBuffer colorData = this.colorData;
        colorData.put(renderData.red).put(renderData.green).put(renderData.blue);
        colorData.clear();
        RenderSystem.glUniform3(this.uniformColor, colorData);

        int flags = renderData.shape;
        GlyphStroke stroke = renderData.stroke;

        if (stroke != null) {
            flags |= STROKE_ACTIVE_BIT;
        }

        if (renderData.highlightNodes) {
            flags |= HIGHLIGHT_NODES_BIT;
        }

        RenderSystem.glUniform1i(this.uniformFlags, flags);

        FloatBuffer strokeData = this.strokeData;
        if (stroke != null) {
            stroke.writeToBuffer(strokeData, tickDelta);
            strokeData.clear();
            RenderSystem.glUniform4(this.uniformStroke, strokeData);
        }
    }

    public void unbind() {
        GlProgramManager.useProgram(0);
    }

    @Override
    public void close() {
        this.program.close();

        MemoryUtil.memFree(this.colorData);
        MemoryUtil.memFree(this.strokeData);
    }
}
