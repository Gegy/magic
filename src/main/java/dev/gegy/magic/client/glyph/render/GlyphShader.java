package dev.gegy.magic.client.glyph.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.Magic;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

public final class GlyphShader implements AutoCloseable {
    private static final Identifier ID = Magic.identifier("glyph");

    private static final String ATTRIBUTE_POSITION = "Position";

    private static final String UNIFORM_GLYPH_TO_WORLD = "glyph_to_world";
    private static final String UNIFORM_WORLD_TO_SCREEN = "world_to_screen";
    private static final String UNIFORM_CENTER = "center";
    private static final String UNIFORM_RADIUS = "radius";
    private static final String UNIFORM_COLOR = "color";
    private static final String UNIFORM_EDGES = "edges";

    private final Program program;

    private final int attributePosition;
    private final int uniformGlyphToWorld;
    private final int uniformWorldToScreen;
    private final int uniformCenter;
    private final int uniformRadius;
    private final int uniformColor;
    private final int uniformEdges;

    private final FloatBuffer glyphToWorldData = MemoryUtil.memAllocFloat(4 * 4);
    private final FloatBuffer worldToScreenData = MemoryUtil.memAllocFloat(4 * 4);
    private final FloatBuffer centerData = MemoryUtil.memAllocFloat(2);
    private final FloatBuffer colorData = MemoryUtil.memAllocFloat(3);

    private GlyphShader(Program program, int attributePosition, int uniformGlyphToWorld, int uniformWorldToScreen, int uniformCenter, int uniformRadius, int uniformColor, int uniformEdges) {
        this.program = program;
        this.attributePosition = attributePosition;
        this.uniformGlyphToWorld = uniformGlyphToWorld;
        this.uniformWorldToScreen = uniformWorldToScreen;
        this.uniformCenter = uniformCenter;
        this.uniformRadius = uniformRadius;
        this.uniformColor = uniformColor;
        this.uniformEdges = uniformEdges;
    }

    public static GlyphShader create(ResourceManager resources) throws IOException {
        GlShader vertexShader = compileShader(resources, GlShader.Type.VERTEX);
        GlShader fragmentShader = compileShader(resources, GlShader.Type.FRAGMENT);

        Program program = new Program(GlProgramManager.createProgram(), vertexShader, fragmentShader);
        GlProgramManager.linkProgram(program);

        int attributePosition = program.getAttributeLocation(ATTRIBUTE_POSITION);
        int uniformGlyphToWorld = program.getUniformLocation(UNIFORM_GLYPH_TO_WORLD);
        int uniformWorldToScreen = program.getUniformLocation(UNIFORM_WORLD_TO_SCREEN);
        int uniformCenter = program.getUniformLocation(UNIFORM_CENTER);
        int uniformRadius = program.getUniformLocation(UNIFORM_RADIUS);
        int uniformColor = program.getUniformLocation(UNIFORM_COLOR);
        int uniformEdges = program.getUniformLocation(UNIFORM_EDGES);

        return new GlyphShader(program, attributePosition, uniformGlyphToWorld, uniformWorldToScreen, uniformCenter, uniformRadius, uniformColor, uniformEdges);
    }

    private static GlShader compileShader(ResourceManager resources, GlShader.Type type) throws IOException {
        Identifier path = new Identifier(ID.getNamespace(), "shaders/" + ID.getPath() + type.getFileExtension());
        try (Resource resource = resources.getResource(path)) {
            return GlShader.createFromResource(type, ID.toString(), resource.getInputStream(), resource.getResourcePackName());
        }
    }

    public void bind(Matrix4f worldToScreen) {
        GlProgramManager.useProgram(this.program.getProgramRef());

        FloatBuffer worldToScreenData = this.worldToScreenData;
        worldToScreen.writeToBuffer(worldToScreenData);
        worldToScreenData.clear();

        RenderSystem.glUniformMatrix4(this.uniformWorldToScreen, false, worldToScreenData);
    }

    public void set(Matrix4f glyphToWorld, float centerX, float centerY, float radius, float red, float green, float blue, int edges) {
        FloatBuffer glyphToWorldData = this.glyphToWorldData;
        glyphToWorld.writeToBuffer(glyphToWorldData);
        glyphToWorldData.clear();
        RenderSystem.glUniformMatrix4(this.uniformGlyphToWorld, false, glyphToWorldData);

        FloatBuffer centerData = this.centerData;
        centerData.put(centerX).put(centerY);
        centerData.clear();
        RenderSystem.glUniform2(this.uniformCenter, centerData);

        GL20.glUniform1f(this.uniformRadius, radius);

        FloatBuffer colorData = this.colorData;
        colorData.put(red).put(green).put(blue);
        colorData.clear();
        RenderSystem.glUniform3(this.uniformColor, colorData);

        RenderSystem.glUniform1i(this.uniformEdges, edges);
    }

    public void unbind() {
        GlProgramManager.useProgram(0);
    }

    @Override
    public void close() {
        this.program.close();

        MemoryUtil.memFree(this.glyphToWorldData);
        MemoryUtil.memFree(this.worldToScreenData);
        MemoryUtil.memFree(this.centerData);
        MemoryUtil.memFree(this.colorData);
    }

    static final class Program implements GlProgram, AutoCloseable {
        final int programRef;
        final GlShader vertexShader;
        final GlShader fragmentShader;

        Program(int programRef, GlShader vertexShader, GlShader fragmentShader) {
            this.programRef = programRef;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        int getAttributeLocation(String name) {
            return GlStateManager.getAttribLocation(this.programRef, name);
        }

        int getUniformLocation(String name) {
            return GlStateManager.getUniformLocation(this.programRef, name);
        }

        @Override
        public int getProgramRef() {
            return this.programRef;
        }

        @Override
        public GlShader getVertexShader() {
            return this.vertexShader;
        }

        @Override
        public GlShader getFragmentShader() {
            return this.fragmentShader;
        }

        @Override
        public void markUniformsDirty() {
        }

        @Override
        public void close() {
            GlProgramManager.deleteProgram(this);
        }
    }
}
