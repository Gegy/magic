package dev.gegy.magic.client.render.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.EffectShader;
import dev.gegy.magic.client.render.shader.RenderEffect;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.function.Function;

final class BeamEndWorldEffect implements RenderEffect<BeamRenderData> {
    private final EffectShader shader;

    private final int uniformModelViewProject;
    private final int uniformSampler;
    private final int uniformScale;

    private final Function<BeamRenderData, Matrix4f> modelViewProject;

    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private BeamEndWorldEffect(
            EffectShader shader,
            int uniformModelViewProject,
            int uniformSampler,
            int uniformScale,
            Function<BeamRenderData, Matrix4f> modelViewProject
    ) {
        this.shader = shader;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformSampler = uniformSampler;
        this.uniformScale = uniformScale;
        this.modelViewProject = modelViewProject;
    }

    public static BeamEndWorldEffect create(ResourceManager resources, Function<BeamRenderData, Matrix4f> modelViewProject) throws IOException {
        EffectShader shader = EffectShader.compile(
                resources,
                Magic.identifier("beam/end_world"),
                Magic.identifier("effect_world"),
                MagicGeometry.POSITION_2F
        );

        int uniformModelViewProject = shader.getUniformLocation("ModelViewProject");
        int uniformSampler = shader.getUniformLocation("Sampler");
        int uniformScale = shader.getUniformLocation("Scale");

        return new BeamEndWorldEffect(
                shader,
                uniformModelViewProject,
                uniformSampler,
                uniformScale,
                modelViewProject
        );
    }

    @Override
    public void bind(BeamRenderData renderData) {
        this.shader.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        this.modelViewProject.apply(renderData).writeToBuffer(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        GL20.glUniform1f(this.uniformScale, BeamTexture.END_SCALE * renderData.sourceRadius);
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
