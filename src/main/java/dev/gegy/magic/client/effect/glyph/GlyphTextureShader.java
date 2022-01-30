package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.render.GeometryBuilder;
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
    private final int uniformOpacity;
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
            int uniformOpacity,
            int uniformPrimaryColor, int uniformSecondaryColor,
            int uniformFlags, int uniformStroke
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

    public static GlyphTextureShader create(ResourceManager resources) throws IOException {
        EffectShaderProgram program = EffectShaderProgram.compile(resources, Magic.identifier("glyph/texture"), GeometryBuilder.POSITION_2F);

        int uniformTexelSize = program.getUniformLocation("TexelSize");
        int uniformRenderSize = program.getUniformLocation("RenderSize");
        int uniformOpacity = program.getUniformLocation("Opacity");
        int uniformPrimaryColor = program.getUniformLocation("PrimaryColor");
        int uniformSecondaryColor = program.getUniformLocation("SecondaryColor");
        int uniformFlags = program.getUniformLocation("Flags");
        int uniformStroke = program.getUniformLocation("Stroke");

        return new GlyphTextureShader(
                program,
                uniformTexelSize, uniformRenderSize,
                uniformOpacity,
                uniformPrimaryColor, uniformSecondaryColor,
                uniformFlags, uniformStroke
        );
    }

    @Override
    public void bind(GlyphRenderParameters parameters) {
        this.program.bind();

        GL20.glUniform1f(this.uniformTexelSize, GlyphTexture.TEXEL_SIZE);
        GL20.glUniform1f(this.uniformRenderSize, GlyphTexture.RENDER_SIZE);

        GL20.glUniform1f(this.uniformOpacity, parameters.opacity);

        FloatBuffer primaryColorData = this.primaryColorData;
        primaryColorData.put(parameters.primaryRed).put(parameters.primaryGreen).put(parameters.primaryBlue);
        primaryColorData.clear();
        RenderSystem.glUniform3(this.uniformPrimaryColor, primaryColorData);

        FloatBuffer secondaryColorData = this.secondaryColorData;
        secondaryColorData.put(parameters.secondaryRed).put(parameters.secondaryGreen).put(parameters.secondaryBlue);
        secondaryColorData.clear();
        RenderSystem.glUniform3(this.uniformSecondaryColor, secondaryColorData);

        RenderSystem.glUniform1i(this.uniformFlags, this.getGlyphFlags(parameters));

        GlyphStroke stroke = parameters.stroke;
        if (stroke != null) {
            FloatBuffer strokeData = this.strokeData;
            stroke.writeToBuffer(strokeData);
            strokeData.clear();
            RenderSystem.glUniform4(this.uniformStroke, strokeData);
        }
    }

    private int getGlyphFlags(GlyphRenderParameters parameters) {
        int flags = parameters.shape;
        if (parameters.stroke != null) flags |= STROKE_ACTIVE_BIT;
        if (parameters.highlightNodes) flags |= HIGHLIGHT_NODES_BIT;

        return flags;
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
