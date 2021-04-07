package dev.gegy.magic.client.render.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.RenderEffect;
import dev.gegy.magic.client.render.shader.EffectShader;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class GlyphWorldEffect implements RenderEffect<GlyphRenderData> {
    private final EffectShader shader;

    private final int uniformModelViewProject;
    private final int uniformScale;
    private final int uniformDistance;
    private final int uniformSampler;

    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private GlyphWorldEffect(
            EffectShader shader,
            int uniformModelViewProject,
            int uniformScale,
            int uniformDistance,
            int uniformSampler
    ) {
        this.shader = shader;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformScale = uniformScale;
        this.uniformDistance = uniformDistance;
        this.uniformSampler = uniformSampler;
    }

    public static GlyphWorldEffect create(ResourceManager resources) throws IOException {
        EffectShader shader = EffectShader.compile(resources, Magic.identifier("glyph/world"), Magic.identifier("effect_world"), MagicGeometry.POSITION_2F);

        int uniformModelViewProject = shader.getUniformLocation("ModelViewProject");
        int uniformScale = shader.getUniformLocation("Scale");
        int uniformDistance = shader.getUniformLocation("Distance");
        int uniformSampler = shader.getUniformLocation("Sampler");

        return new GlyphWorldEffect(
                shader,
                uniformModelViewProject,
                uniformScale,
                uniformDistance,
                uniformSampler
        );
    }

    @Override
    public void bind(GlyphRenderData renderData) {
        this.shader.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        renderData.modelViewProject.writeToBuffer(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        GL20.glUniform1f(this.uniformScale, renderData.radius * GlyphTexture.RENDER_SCALE);
        GL20.glUniform1f(this.uniformDistance, renderData.distance);
    }

    @Override
    public void unbind() {
        this.shader.unbind();
    }

    @Override
    public void close() {
        this.shader.close();

        MemoryUtil.memFree(this.modelViewProjectData);
    }
}
