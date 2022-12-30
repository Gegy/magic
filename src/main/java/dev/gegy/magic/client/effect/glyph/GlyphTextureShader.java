package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlBinding;
import net.minecraft.server.packs.resources.ResourceManager;
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
    private final int uniformOpacity;
    private final int uniformPrimaryColor;
    private final int uniformSecondaryColor;
    private final int uniformFlags;
    private final int uniformStroke;

    private final FloatBuffer primaryColorData = MemoryUtil.memAllocFloat(3);
    private final FloatBuffer secondaryColorData = MemoryUtil.memAllocFloat(3);
    private final FloatBuffer strokeData = MemoryUtil.memAllocFloat(4);

    private GlyphTextureShader(
            final EffectShaderProgram program,
            final int uniformTexelSize, final int uniformRenderSize,
            final int uniformOpacity,
            final int uniformPrimaryColor, final int uniformSecondaryColor,
            final int uniformFlags, final int uniformStroke
    ) {
        this.program = program;
        this.uniformTexelSize = uniformTexelSize;
        this.uniformRenderSize = uniformRenderSize;
        this.uniformOpacity = uniformOpacity;
        this.uniformPrimaryColor = uniformPrimaryColor;
        this.uniformSecondaryColor = uniformSecondaryColor;
        this.uniformFlags = uniformFlags;
        this.uniformStroke = uniformStroke;
    }

    public static GlyphTextureShader create(final ResourceManager resources) throws IOException {
        final EffectShaderProgram program = EffectShaderProgram.compile(resources, Magic.identifier("glyph/texture"), GeometryBuilder.POSITION_2F);

        final int uniformTexelSize = program.getUniformLocation("TexelSize");
        final int uniformRenderSize = program.getUniformLocation("RenderSize");
        final int uniformOpacity = program.getUniformLocation("Opacity");
        final int uniformPrimaryColor = program.getUniformLocation("PrimaryColor");
        final int uniformSecondaryColor = program.getUniformLocation("SecondaryColor");
        final int uniformFlags = program.getUniformLocation("Flags");
        final int uniformStroke = program.getUniformLocation("Stroke");

        return new GlyphTextureShader(
                program,
                uniformTexelSize, uniformRenderSize,
                uniformOpacity,
                uniformPrimaryColor, uniformSecondaryColor,
                uniformFlags, uniformStroke
        );
    }

    @Override
    public GlBinding bind(final GlyphRenderParameters parameters) {
        final EffectShaderProgram.Binding binding = program.bind();

        GL20.glUniform1f(uniformTexelSize, GlyphTexture.TEXEL_SIZE);
        GL20.glUniform1f(uniformRenderSize, GlyphTexture.RENDER_SIZE);

        GL20.glUniform1f(uniformOpacity, parameters.opacity);

        final FloatBuffer primaryColorData = this.primaryColorData;
        primaryColorData.put(parameters.primaryRed).put(parameters.primaryGreen).put(parameters.primaryBlue);
        primaryColorData.clear();
        RenderSystem.glUniform3(uniformPrimaryColor, primaryColorData);

        final FloatBuffer secondaryColorData = this.secondaryColorData;
        secondaryColorData.put(parameters.secondaryRed).put(parameters.secondaryGreen).put(parameters.secondaryBlue);
        secondaryColorData.clear();
        RenderSystem.glUniform3(uniformSecondaryColor, secondaryColorData);

        RenderSystem.glUniform1i(uniformFlags, getGlyphFlags(parameters));

        final GlyphStroke stroke = parameters.stroke;
        if (stroke != null) {
            final FloatBuffer strokeData = this.strokeData;
            stroke.writeToBuffer(strokeData);
            strokeData.clear();
            RenderSystem.glUniform4(uniformStroke, strokeData);
        }

        return binding;
    }

    private int getGlyphFlags(final GlyphRenderParameters parameters) {
        int flags = parameters.shape;
        if (parameters.stroke != null) flags |= STROKE_ACTIVE_BIT;
        if (parameters.highlightNodes) flags |= HIGHLIGHT_NODES_BIT;

        return flags;
    }

    @Override
    public void delete() {
        program.delete();

        MemoryUtil.memFree(primaryColorData);
        MemoryUtil.memFree(secondaryColorData);
        MemoryUtil.memFree(strokeData);
    }
}
