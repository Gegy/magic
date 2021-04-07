package dev.gegy.magic.client.render.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.EffectShader;
import dev.gegy.magic.client.render.shader.RenderEffect;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

final class BeamTextureEffect implements RenderEffect<BeamRenderData> {
    private final EffectShader shader;

    private final int uniformScale;
    private final int uniformColor;
    private final int uniformTime;

    private final FloatBuffer scaleData = MemoryUtil.memAllocFloat(2);
    private final FloatBuffer colorData = MemoryUtil.memAllocFloat(3);

    private BeamTextureEffect(
            EffectShader shader,
            int uniformScale,
            int uniformColor,
            int uniformTime,
            float scaleX, float scaleY
    ) {
        this.shader = shader;
        this.uniformScale = uniformScale;
        this.uniformColor = uniformColor;
        this.uniformTime = uniformTime;

        this.scaleData.put(scaleX).put(scaleY);
        this.scaleData.clear();
    }

    public static BeamTextureEffect create(ResourceManager resources) throws IOException {
        return create(resources, "beam/texture", "beam/texture", BeamTexture.SCALE_X, BeamTexture.SCALE_Y);
    }

    public static BeamTextureEffect createImpact(ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/impact_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    public static BeamTextureEffect createCloud(ResourceManager resources) throws IOException {
        return create(resources, "beam/end_texture", "beam/cloud_texture", BeamTexture.END_SCALE, BeamTexture.END_SCALE);
    }

    private static BeamTextureEffect create(
            ResourceManager resources, String vertexPath, String fragmentPath,
            float scaleX, float scaleY
    ) throws IOException {
        EffectShader shader = EffectShader.compile(
                resources, Magic.identifier(vertexPath), Magic.identifier(fragmentPath),
                MagicGeometry.POSITION_2F
        );

        int uniformScale = shader.getUniformLocation("Scale");
        int uniformColor = shader.getUniformLocation("Color");
        int uniformTime = shader.getUniformLocation("Time");

        return new BeamTextureEffect(
                shader,
                uniformScale,
                uniformColor,
                uniformTime,
                scaleX, scaleY
        );
    }

    @Override
    public void bind(BeamRenderData renderData) {
        this.shader.bind();

        RenderSystem.glUniform2(this.uniformScale, this.scaleData);

        FloatBuffer colorData = this.colorData;
        colorData.put(renderData.red).put(renderData.green).put(renderData.blue);
        colorData.clear();
        RenderSystem.glUniform3(this.uniformColor, colorData);

        GL20.glUniform1f(this.uniformTime, renderData.time);
    }

    @Override
    public void unbind() {
        this.shader.unbind();
    }

    @Override
    public void close() {
        this.shader.close();

        MemoryUtil.memFree(this.scaleData);
        MemoryUtil.memFree(this.colorData);
    }
}
