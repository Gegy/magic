package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.render.GeometryBuilder;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class GlyphWorldShader implements EffectShader<GlyphRenderParameters> {
    private final EffectShaderProgram program;

    private final int uniformModelViewProject;
    private final int uniformScale;
    private final int uniformSampler;

    private final FloatBuffer modelViewProjectData = MemoryUtil.memAllocFloat(4 * 4);

    private GlyphWorldShader(
            EffectShaderProgram program,
            int uniformModelViewProject,
            int uniformScale,
            int uniformSampler
    ) {
        this.program = program;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformScale = uniformScale;
        this.uniformSampler = uniformSampler;
    }

    public static GlyphWorldShader create(ResourceManager resources) throws IOException {
        EffectShaderProgram program = EffectShaderProgram.compile(resources, Magic.identifier("glyph/world"), Magic.identifier("effect_world"), GeometryBuilder.POSITION_2F);

        int uniformModelViewProject = program.getUniformLocation("ModelViewProject");
        int uniformScale = program.getUniformLocation("Scale");
        int uniformSampler = program.getUniformLocation("Sampler");

        return new GlyphWorldShader(
                program,
                uniformModelViewProject,
                uniformScale,
                uniformSampler
        );
    }

    @Override
    public void bind(GlyphRenderParameters parameters) {
        this.program.bind();

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer modelViewProjectData = this.modelViewProjectData;
        parameters.modelViewProject.writeColumnMajor(modelViewProjectData);
        modelViewProjectData.clear();
        RenderSystem.glUniformMatrix4(this.uniformModelViewProject, false, modelViewProjectData);

        GL20.glUniform1f(this.uniformScale, parameters.radius * GlyphTexture.RENDER_SCALE);
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
