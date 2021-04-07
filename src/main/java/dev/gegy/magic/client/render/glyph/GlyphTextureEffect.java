package dev.gegy.magic.client.render.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.RenderEffect;
import dev.gegy.magic.client.render.shader.EffectShader;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class GlyphTextureEffect implements RenderEffect<GlyphRenderData> {
    private static final int STROKE_ACTIVE_BIT = 1 << 15;
    private static final int HIGHLIGHT_NODES_BIT = 1 << 16;

    private final EffectShader shader;

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

    private GlyphTextureEffect(
            EffectShader shader,
            int uniformTexelSize, int uniformRenderSize,
            int uniformFormProgress,
            int uniformPrimaryColor, int uniformSecondaryColor,
            int uniformFlags, int uniformStroke
    ) {
        this.shader = shader;
        this.uniformTexelSize = uniformTexelSize;
        this.uniformRenderSize = uniformRenderSize;
        this.uniformFormProgress = uniformFormProgress;
        this.uniformPrimaryColor = uniformPrimaryColor;
        this.uniformSecondaryColor = uniformSecondaryColor;
        this.uniformFlags = uniformFlags;
        this.uniformStroke = uniformStroke;
    }

    public static GlyphTextureEffect create(ResourceManager resources) throws IOException {
        EffectShader shader = EffectShader.compile(resources, Magic.identifier("glyph/texture"), MagicGeometry.POSITION_2F);

        int uniformTexelSize = shader.getUniformLocation("TexelSize");
        int uniformRenderSize = shader.getUniformLocation("RenderSize");
        int uniformFormProgress = shader.getUniformLocation("FormProgress");
        int uniformPrimaryColor = shader.getUniformLocation("PrimaryColor");
        int uniformSecondaryColor = shader.getUniformLocation("SecondaryColor");
        int uniformFlags = shader.getUniformLocation("Flags");
        int uniformStroke = shader.getUniformLocation("Stroke");

        return new GlyphTextureEffect(
                shader,
                uniformTexelSize, uniformRenderSize,
                uniformFormProgress,
                uniformPrimaryColor, uniformSecondaryColor,
                uniformFlags, uniformStroke
        );
    }

    @Override
    public void bind(GlyphRenderData renderData) {
        this.shader.bind();

        GL20.glUniform1f(this.uniformTexelSize, GlyphTexture.TEXEL_SIZE);
        GL20.glUniform1f(this.uniformRenderSize, GlyphTexture.RENDER_SIZE);

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
            stroke.writeToBuffer(strokeData, renderData.tickDelta);
            strokeData.clear();
            RenderSystem.glUniform4(this.uniformStroke, strokeData);
        }
    }

    @Override
    public void unbind() {
        this.shader.unbind();
    }

    @Override
    public void close() {
        this.shader.close();

        MemoryUtil.memFree(this.primaryColorData);
        MemoryUtil.memFree(this.secondaryColorData);
        MemoryUtil.memFree(this.strokeData);
    }
}
