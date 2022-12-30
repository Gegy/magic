package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectShaderProgram;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlBinding;
import net.minecraft.server.packs.resources.ResourceManager;
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
            final EffectShaderProgram program,
            final int uniformModelViewProject,
            final int uniformScale,
            final int uniformSampler
    ) {
        this.program = program;
        this.uniformModelViewProject = uniformModelViewProject;
        this.uniformScale = uniformScale;
        this.uniformSampler = uniformSampler;
    }

    public static GlyphWorldShader create(final ResourceManager resources) throws IOException {
        final EffectShaderProgram program = EffectShaderProgram.compile(resources, Magic.identifier("glyph/world"), Magic.identifier("effect_world"), GeometryBuilder.POSITION_2F);

        final int uniformModelViewProject = program.getUniformLocation("ModelViewProject");
        final int uniformScale = program.getUniformLocation("Scale");
        final int uniformSampler = program.getUniformLocation("Sampler");

        return new GlyphWorldShader(
                program,
                uniformModelViewProject,
                uniformScale,
                uniformSampler
        );
    }

    @Override
    public GlBinding bind(final GlyphRenderParameters parameters) {
        final EffectShaderProgram.Binding binding = program.bind();

        RenderSystem.glUniform1i(uniformSampler, 0);

        RenderSystem.glUniformMatrix4(uniformModelViewProject, false,
                parameters.modelViewProject.get(modelViewProjectData)
        );

        GL20.glUniform1f(uniformScale, parameters.radius * GlyphTexture.RENDER_SCALE);

        return binding;
    }

    @Override
    public void delete() {
        program.delete();

        MemoryUtil.memFree(modelViewProjectData);
    }
}
