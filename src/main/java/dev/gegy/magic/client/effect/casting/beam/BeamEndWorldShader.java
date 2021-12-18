package dev.gegy.magic.client.effect.casting.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.render.GeometryBuilder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.function.Function;

final class BeamEndWorldShader implements EffectShader<BeamRenderParameters> {
    private final EffectShaderProgram program;

    private final int uniformModelViewProject;
    private final int uniformSampler;
    private final int uniformScale;

    private final Function<BeamRenderParameters, Matrix4f> modelViewProject;

    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private BeamEndWorldShader(
            EffectShaderProgram program,
            int uniformModelViewProject,
            int uniformSampler,
            int uniformScale,
            Function<BeamRenderParameters, Matrix4f> modelViewProject
    ) {
        this.program = program;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformSampler = uniformSampler;
        this.uniformScale = uniformScale;
        this.modelViewProject = modelViewProject;
    }

    public static BeamEndWorldShader create(ResourceManager resources, Function<BeamRenderParameters, Matrix4f> modelViewProject) throws IOException {
        EffectShaderProgram program = EffectShaderProgram.compile(
                resources,
                Magic.identifier("beam/end_world"),
                Magic.identifier("effect_world"),
                GeometryBuilder.POSITION_2F
        );

        int uniformModelViewProject = program.getUniformLocation("ModelViewProject");
        int uniformSampler = program.getUniformLocation("Sampler");
        int uniformScale = program.getUniformLocation("Scale");

        return new BeamEndWorldShader(
                program,
                uniformModelViewProject,
                uniformSampler,
                uniformScale,
                modelViewProject
        );
    }

    @Override
    public void bind(BeamRenderParameters parameters) {
        this.program.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        this.modelViewProject.apply(parameters).writeColumnMajor(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        GL20.glUniform1f(this.uniformScale, BeamTexture.END_SCALE * parameters.sourceRadius);
    }

    @Override
    public void unbind() {
        this.program.unbind();
    }

    @Override
    public void close() {
        this.program.close();

        MemoryUtil.memFree(this.modelViewProjectData);
    }
}
