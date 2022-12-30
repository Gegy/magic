package dev.gegy.magic.client.effect.casting.spell.beam.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.render.gl.GlBinding;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class BeamWorldShader implements EffectShader<BeamRenderParameters> {
    private final EffectShaderProgram program;

    private final int uniformModelViewProject;
    private final int uniformSampler;
    private final int uniformScale;

    private final FloatBuffer scaleData = MemoryUtil.memAllocFloat(2);
    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private BeamWorldShader(
            final EffectShaderProgram program,
            final int uniformModelViewProject,
            final int uniformSampler,
            final int uniformScale
    ) {
        this.program = program;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformSampler = uniformSampler;
        this.uniformScale = uniformScale;
    }

    public static BeamWorldShader create(final ResourceManager resources) throws IOException {
        final EffectShaderProgram program = EffectShaderProgram.compile(
                resources,
                Magic.identifier("beam/world"),
                Magic.identifier("effect_world"),
                DefaultVertexFormat.POSITION_TEX
        );

        final int uniformModelViewProject = program.getUniformLocation("ModelViewProject");
        final int uniformSampler = program.getUniformLocation("Sampler");
        final int uniformScale = program.getUniformLocation("Scale");

        return new BeamWorldShader(
                program,
                uniformModelViewProject,
                uniformSampler,
                uniformScale
        );
    }

    @Override
    public GlBinding bind(final BeamRenderParameters parameters) {
        final EffectShaderProgram.Binding binding = program.bind();

        RenderSystem.glUniform1i(uniformSampler, 0);

        RenderSystem.glUniformMatrix4(uniformModelViewProject, false,
                parameters.modelViewProject.get(modelViewProjectData)
        );
        RenderSystem.glUniform2(uniformScale,
                scaleData.put(0, BeamTexture.SCALE_X).put(1, BeamTexture.SCALE_Y)
        );

        return binding;
    }

    @Override
    public void delete() {
        program.delete();

        MemoryUtil.memFree(scaleData);
        MemoryUtil.memFree(modelViewProjectData);
    }
}
