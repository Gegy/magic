package dev.gegy.magic.client.render.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.shader.EffectShader;
import dev.gegy.magic.client.render.shader.RenderEffect;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class BeamWorldEffect implements RenderEffect<BeamRenderData> {
    private final EffectShader shader;

    private final int uniformModelViewProject;
    private final int uniformSampler;
    private final int uniformScale;
    private final int uniformDistance;

    private final FloatBuffer scaleData = MemoryUtil.memAllocFloat(2);
    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private BeamWorldEffect(
            EffectShader shader,
            int uniformModelViewProject,
            int uniformSampler,
            int uniformScale,
            int uniformDistance
    ) {
        this.shader = shader;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformSampler = uniformSampler;
        this.uniformScale = uniformScale;
        this.uniformDistance = uniformDistance;
    }

    public static BeamWorldEffect create(ResourceManager resources) throws IOException {
        EffectShader shader = EffectShader.compile(
                resources,
                Magic.identifier("beam/world"),
                Magic.identifier("effect_world"),
                VertexFormats.POSITION_TEXTURE
        );

        int uniformModelViewProject = shader.getUniformLocation("ModelViewProject");
        int uniformSampler = shader.getUniformLocation("Sampler");
        int uniformScale = shader.getUniformLocation("Scale");
        int uniformDistance = shader.getUniformLocation("Distance");

        return new BeamWorldEffect(
                shader,
                uniformModelViewProject,
                uniformSampler,
                uniformScale,
                uniformDistance
        );
    }

    @Override
    public void bind(BeamRenderData renderData) {
        this.shader.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        renderData.modelViewProject.writeToBuffer(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        FloatBuffer scaleData = this.scaleData;
        scaleData.put(BeamTexture.SCALE_X * renderData.sourceRadius).put(BeamTexture.SCALE_Y * renderData.sourceRadius);
        scaleData.clear();
        RenderSystem.glUniform2(this.uniformScale, scaleData);

        GL20.glUniform1f(this.uniformDistance, renderData.distance);
    }

    @Override
    public void unbind() {
        this.shader.unbind();
    }

    @Override
    public void close() {
        this.shader.close();

        MemoryUtil.memFree(this.scaleData);
        MemoryUtil.memFree(this.modelViewProjectData);
    }
}
