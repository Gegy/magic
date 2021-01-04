package dev.gegy.magic.client.glyph.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.render.GlyphRenderData;
import dev.gegy.magic.client.glyph.render.GlyphTexture;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

public final class GlyphWorldShader implements AutoCloseable {
    private final GlyphShaderProgram program;

    private final int uniformGlyphToWorld;
    private final int uniformWorldToScreen;
    private final int uniformRenderScale;
    private final int uniformRadius;
    private final int uniformSampler;

    private final FloatBuffer glyphToWorldData = MemoryUtil.memAllocFloat(4 * 4);
    private final FloatBuffer worldToScreenData = MemoryUtil.memAllocFloat(4 * 4);

    private GlyphWorldShader(
            GlyphShaderProgram program,
            int uniformGlyphToWorld, int uniformWorldToScreen,
            int uniformRenderScale,
            int uniformRadius,
            int uniformSampler
    ) {
        this.program = program;
        this.uniformGlyphToWorld = uniformGlyphToWorld;
        this.uniformWorldToScreen = uniformWorldToScreen;
        this.uniformRenderScale = uniformRenderScale;
        this.uniformRadius = uniformRadius;
        this.uniformSampler = uniformSampler;
    }

    public static GlyphWorldShader create(ResourceManager resources) throws IOException {
        GlyphShaderProgram program = GlyphShaderProgram.compile(resources, Magic.identifier("glyph_world"));

        int uniformGlyphToWorld = program.getUniformLocation("glyph_to_world");
        int uniformWorldToScreen = program.getUniformLocation("world_to_screen");
        int uniformRenderScale = program.getUniformLocation("render_scale");
        int uniformRadius = program.getUniformLocation("radius");
        int uniformSampler = program.getUniformLocation("sampler");

        return new GlyphWorldShader(
                program,
                uniformGlyphToWorld, uniformWorldToScreen,
                uniformRenderScale,
                uniformRadius,
                uniformSampler
        );
    }

    public void bind(GlyphTexture texture, Matrix4f worldToScreen, GlyphRenderData renderData) {
        GlProgramManager.useProgram(this.program.getProgramRef());

        FloatBuffer worldToScreenData = this.worldToScreenData;
        worldToScreen.writeToBuffer(worldToScreenData);
        worldToScreenData.clear();

        RenderSystem.glUniformMatrix4(this.uniformWorldToScreen, false, worldToScreenData);

        RenderSystem.glUniform1i(this.uniformSampler, 0);

        FloatBuffer glyphToWorldData = this.glyphToWorldData;
        renderData.glyphToWorld.writeToBuffer(glyphToWorldData);
        glyphToWorldData.clear();
        RenderSystem.glUniformMatrix4(this.uniformGlyphToWorld, false, glyphToWorldData);

        GL20.glUniform1f(this.uniformRenderScale, texture.getRenderScale());
        GL20.glUniform1f(this.uniformRadius, renderData.radius);
    }

    public void unbind() {
        GlProgramManager.useProgram(0);
    }

    @Override
    public void close() {
        this.program.close();

        MemoryUtil.memFree(this.glyphToWorldData);
        MemoryUtil.memFree(this.worldToScreenData);
    }
}
