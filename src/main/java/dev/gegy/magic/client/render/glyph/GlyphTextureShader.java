package dev.gegy.magic.client.render.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.render.shader.SimpleShaderProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

public final class GlyphTextureShader implements AutoCloseable {
    private static final int STROKE_ACTIVE_BIT = 1 << 15;
    private static final int HIGHLIGHT_NODES_BIT = 1 << 16;

    private final SimpleShaderProgram program;

    private final int uniformTexelSize;
    private final int uniformRenderSize;
    private final int uniformFormProgress;
    private final int uniformPrimaryColor;
    private final int uniformSecondaryColor;
    private final int uniformFlags;
    private final int uniformStroke;

    private final FloatBuffer primaryColorData = MemoryUtil.memAllocFloat(3);
    private final FloatBuffer secondaryColorData = MemoryUtil.memAllocFloat(3);
    private final FloatBuffer strokeData = MemoryUtil.memAllocFloat(4);

    private GlyphTextureShader(
            SimpleShaderProgram program,
            int uniformTexelSize, int uniformRenderSize,
            int uniformFormProgress,
            int uniformPrimaryColor, int uniformSecondaryColor,
            int uniformFlags, int uniformStroke
    ) {
        this.program = program;
        this.uniformTexelSize = uniformTexelSize;
        this.uniformRenderSize = uniformRenderSize;
        this.uniformFormProgress = uniformFormProgress;
        this.uniformPrimaryColor = uniformPrimaryColor;
        this.uniformSecondaryColor = uniformSecondaryColor;
        this.uniformFlags = uniformFlags;
        this.uniformStroke = uniformStroke;
    }

    public static GlyphTextureShader create(ResourceManager resources) throws IOException {
        SimpleShaderProgram program = SimpleShaderProgram.compile(resources, Magic.identifier("glyph/texture"));

        int uniformTexelSize = program.getUniformLocation("texel_size");
        int uniformRenderSize = program.getUniformLocation("render_size");
        int uniformFormProgress = program.getUniformLocation("form_progress");
        int uniformPrimaryColor = program.getUniformLocation("primary_color");
        int uniformSecondaryColor = program.getUniformLocation("secondary_color");
        int uniformFlags = program.getUniformLocation("flags");
        int uniformStroke = program.getUniformLocation("stroke");

        return new GlyphTextureShader(
                program,
                uniformTexelSize, uniformRenderSize,
                uniformFormProgress,
                uniformPrimaryColor, uniformSecondaryColor,
                uniformFlags, uniformStroke
        );
    }

    public void bind(GlyphTexture texture, GlyphRenderData renderData, float tickDelta) {
        GlProgramManager.useProgram(this.program.getProgramRef());

        GL20.glUniform1f(this.uniformTexelSize, texture.getTexelSize());
        GL20.glUniform1f(this.uniformRenderSize, texture.getRenderSize());

        GL20.glUniform1f(this.uniformFormProgress, renderData.formProgress);

        FloatBuffer primaryColorData = this.primaryColorData;
        primaryColorData.put(renderData.primaryRed).put(renderData.primaryGreen).put(renderData.primaryBlue);
        primaryColorData.clear();
        RenderSystem.glUniform3(this.uniformPrimaryColor, primaryColorData);

        FloatBuffer secondaryColorData = this.secondaryColorData;
        secondaryColorData.put(renderData.secondaryRed).put(renderData.secondaryGreen).put(renderData.secondaryBlue);
        secondaryColorData.clear();
        RenderSystem.glUniform3(this.uniformSecondaryColor, secondaryColorData);

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

        MemoryUtil.memFree(this.primaryColorData);
        MemoryUtil.memFree(this.secondaryColorData);
        MemoryUtil.memFree(this.strokeData);
    }
}
