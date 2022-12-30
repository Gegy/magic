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
            EffectShaderProgram program,
            int uniformScale,
            int uniformColor,
            int uniformTime,
            int uniformLength,
            float scaleX, float scaleY
    ) {
        this.program = program;
        this.uniformScale = uniformScale;
        this.uniformColor = uniformColor;
        this.uniformTime = uniformTime;
        this.uniformLength = uniformLength;

        this.scaleData.put(scaleX).put(scaleY);
        this.scaleData.clear();
    }

    public static BeamTextureShader create(ResourceManager resources) throws IOException {
        return create(resources, "beam/texture", "beam/texture", BeamTexture.SCALE_X, BeamTexture.SCALE_Y);
    }

    public static BeamTextureShader createImpact(ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/impact_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    public static BeamTextureShader createCloud(ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/cloud_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    private static BeamTextureShader create(
            ResourceManager resources, String vertexPath, String fragmentPath,
            float scaleX, float scaleY
    ) throws IOException {
        EffectShaderProgram program = EffectShaderProgram.compile(
                resources, Magic.identifier(vertexPath), Magic.identifier(fragmentPath),
                GeometryBuilder.POSITION_2F
        );

        int uniformScale = program.getUniformLocation("Scale");
        int uniformColor = program.getUniformLocation("Color");
        int uniformTime = program.getUniformLocation("Time");
        int uniformLength = program.getUniformLocation("Length");

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
    public GlBinding bind(BeamRenderParameters parameters) {
        var binding = this.program.bind();

        RenderSystem.glUniform2(this.uniformScale, this.scaleData);

        FloatBuffer colorData = this.colorData;
        colorData.put(parameters.red).put(parameters.green).put(parameters.blue);
        colorData.clear();
        RenderSystem.glUniform3(this.uniformColor, colorData);

        GL20.glUniform1f(this.uniformTime, parameters.time);

        GL20.glUniform1f(this.uniformLength, parameters.length);

        return binding;
    }

    @Override
    public void delete() {
        this.program.delete();

        MemoryUtil.memFree(this.scaleData);
        MemoryUtil.memFree(this.colorData);
    }
}
