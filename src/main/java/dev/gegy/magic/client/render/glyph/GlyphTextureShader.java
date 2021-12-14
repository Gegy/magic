package dev.gegy.magic.client.render.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.shader.EffectShader;
import dev.gegy.magic.client.render.shader.EffectShaderProgram;
import dev.gegy.magic.client.glyph.GlyphStroke;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class GlyphTextureShader implements EffectShader<GlyphRenderParameters> {
    private static final int STROKE_ACTIVE_BIT = 1 << 15;
    private static final int HIGHLIGHT_NODES_BIT = 1 << 16;

    private final EffectShaderProgram program;

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
            EffectShaderProgram program,
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
        EffectShaderProgram program = EffectShaderProgram.compile(resources, Magic.identifier("glyph/texture"), GeometryBuilder.POSITION_2F);

        int uniformTexelSize = program.getUniformLocation("TexelSize");
        int uniformRenderSize = program.getUniformLocation("RenderSize");
        int uniformFormProgress = program.getUniformLocation("FormProgress");
        int uniformPrimaryColor = program.getUniformLocation("PrimaryColor");
        int uniformSecondaryColor = program.getUniformLocation("SecondaryColor");
        int uniformFlags = program.getUniformLocation("Flags");
        int uniformStroke = program.getUniformLocation("Stroke");

        return new GlyphTextureShader(
                program,
                uniformTexelSize, uniformRenderSize,
                uniformFormProgress,
                uniformPrimaryColor, uniformSecondaryColor,
                uniformFlags, uniformStroke
        );
    }

    @Override
    public void bind(GlyphRenderParameters parameters) {
        this.program.bind();

        GL20.glUniform1f(this.uniformTexelSize, GlyphTexture.TEXEL_SIZE);
        GL20.glUniform1f(this.uniformRenderSize, GlyphTexture.RENDER_SIZE);

        GL20.glUniform1f(this.uniformFormProgress, parameters.formProgress);

        FloatBuffer primaryColorData = this.primaryColorData;
        primaryColorData.put(parameters.primaryRed).put(parameters.primaryGreen).put(parameters.primaryBlue);
        primaryColorData.clear();
        RenderSystem.glUniform3(this.uniformPrimaryColor, primaryColorData);

        FloatBuffer secondaryColorData = this.secondaryColorData;
        secondaryColorData.put(parameters.secondaryRed).put(parameters.secondaryGreen).put(parameters.secondaryBlue);
        secondaryColorData.clear();
        RenderSystem.glUniform3(this.uniformSecondaryColor, secondaryColorData);

        int flags = parameters.shape;
        GlyphStroke stroke = parameters.stroke;

        if (stroke != null) {
            flags |= STROKE_ACTIVE_BIT;
        }

        if (parameters.highlightNodes) {
            flags |= HIGHLIGHT_NODES_BIT;
        }

        RenderSystem.glUniform1i(this.uniformFlags, flags);

        FloatBuffer strokeData = this.strokeData;
        if (stroke != null) {
            stroke.writeToBuffer(strokeData, parameters.tickDelta);
            strokeData.clear();
            RenderSystem.glUniform4(this.uniformStroke, strokeData);
        }
    }

    @Override
    public void unbind() {
        this.program.unbind();
    }

    @Override
    public void close() {
        this.program.close();

        MemoryUtil.memFree(this.primaryColorData);
        MemoryUtil.memFree(this.secondaryColorData);
        MemoryUtil.memFree(this.strokeData);
    }
}
