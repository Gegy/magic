package dev.gegy.magic.client.effect.casting.spell.beam.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.render.gl.GlBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
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
            EffectShaderProgram program,
            int uniformModelViewProject,
            int uniformSampler,
            int uniformScale
    ) {
        this.program = program;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformSampler = uniformSampler;
        this.uniformScale = uniformScale;
    }

    public static BeamWorldShader create(ResourceManager resources) throws IOException {
        EffectShaderProgram program = EffectShaderProgram.compile(
                resources,
                Magic.identifier("beam/world"),
                Magic.identifier("effect_world"),
                VertexFormats.POSITION_TEXTURE
        );

        int uniformModelViewProject = program.getUniformLocation("ModelViewProject");
        int uniformSampler = program.getUniformLocation("Sampler");
        int uniformScale = program.getUniformLocation("Scale");

        return new BeamWorldShader(
                program,
                uniformModelViewProject,
                uniformSampler,
                uniformScale
        );
    }

    @Override
    public GlBinding bind(BeamRenderParameters parameters) {
        var binding = this.program.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        parameters.modelViewProject.writeColumnMajor(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        FloatBuffer scaleData = this.scaleData;
        scaleData.put(BeamTexture.SCALE_X).put(BeamTexture.SCALE_Y);
        scaleData.clear();
        RenderSystem.glUniform2(this.uniformScale, scaleData);

        return binding;
    }

    @Override
    public void delete() {
        this.program.delete();

        MemoryUtil.memFree(this.scaleData);
        MemoryUtil.memFree(this.modelViewProjectData);
    }
}
