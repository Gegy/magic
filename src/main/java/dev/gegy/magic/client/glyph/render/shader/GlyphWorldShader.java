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
    private static final String UNIFORM_GLYPH_TO_WORLD = "glyph_to_world";
    private static final String UNIFORM_WORLD_TO_SCREEN = "world_to_screen";
    private static final String UNIFORM_RENDER_SCALE = "render_scale";
    private static final String UNIFORM_RADIUS = "radius";
    private static final String UNIFORM_SAMPLER = "sampler";

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

        int uniformGlyphToWorld = program.getUniformLocation(UNIFORM_GLYPH_TO_WORLD);
        int uniformWorldToScreen = program.getUniformLocation(UNIFORM_WORLD_TO_SCREEN);
        int uniformRenderScale = program.getUniformLocation(UNIFORM_RENDER_SCALE);
        int uniformRadius = program.getUniformLocation(UNIFORM_RADIUS);
        int uniformSampler = program.getUniformLocation(UNIFORM_SAMPLER);

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
