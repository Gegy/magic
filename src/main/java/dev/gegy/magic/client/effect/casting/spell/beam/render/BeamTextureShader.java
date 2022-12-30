package dev.gegy.magic.client.effect.casting.spell.beam.render;

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

final class BeamTextureShader implements EffectShader<BeamRenderParameters> {
    private final EffectShaderProgram program;

    private final int uniformScale;
    private final int uniformColor;
    private final int uniformTime;
    private final int uniformLength;

    private final FloatBuffer scaleData = MemoryUtil.memAllocFloat(2);
    private final FloatBuffer colorData = MemoryUtil.memAllocFloat(3);

    private BeamTextureShader(
            final EffectShaderProgram program,
            final int uniformScale,
            final int uniformColor,
            final int uniformTime,
            final int uniformLength,
            final float scaleX, final float scaleY
    ) {
        this.program = program;
        this.uniformScale = uniformScale;
        this.uniformColor = uniformColor;
        this.uniformTime = uniformTime;
        this.uniformLength = uniformLength;

        scaleData.put(scaleX).put(scaleY);
        scaleData.clear();
    }

    public static BeamTextureShader create(final ResourceManager resources) throws IOException {
        return create(resources, "beam/texture", "beam/texture", BeamTexture.SCALE_X, BeamTexture.SCALE_Y);
    }

    public static BeamTextureShader createImpact(final ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/impact_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    public static BeamTextureShader createCloud(final ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/cloud_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    private static BeamTextureShader create(
            final ResourceManager resources, final String vertexPath, final String fragmentPath,
            final float scaleX, final float scaleY
    ) throws IOException {
        final EffectShaderProgram program = EffectShaderProgram.compile(
                resources, Magic.identifier(vertexPath), Magic.identifier(fragmentPath),
                GeometryBuilder.POSITION_2F
        );

        final int uniformScale = program.getUniformLocation("Scale");
        final int uniformColor = program.getUniformLocation("Color");
        final int uniformTime = program.getUniformLocation("Time");
        final int uniformLength = program.getUniformLocation("Length");

        return new BeamTextureShader(
                program,
                uniformScale,
                uniformColor,
                uniformTime,
                uniformLength,
                scaleX, scaleY
        );
    }

    @Override
    public GlBinding bind(final BeamRenderParameters parameters) {
        final EffectShaderProgram.Binding binding = program.bind();

        RenderSystem.glUniform2(uniformScale, scaleData);

        final FloatBuffer colorData = this.colorData;
        colorData.put(parameters.red).put(parameters.green).put(parameters.blue);
        colorData.clear();
        RenderSystem.glUniform3(uniformColor, colorData);

        GL20.glUniform1f(uniformTime, parameters.time);

        GL20.glUniform1f(uniformLength, parameters.length);

        return binding;
    }

    @Override
    public void delete() {
        program.delete();

        MemoryUtil.memFree(scaleData);
        MemoryUtil.memFree(colorData);
    }
}
