package dev.gegy.magic.client.glyph.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

final class GlyphShaderProgram implements GlProgram, AutoCloseable {
    final int programRef;
    final GlShader vertexShader;
    final GlShader fragmentShader;

    private GlyphShaderProgram(int programRef, GlShader vertexShader, GlShader fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    static GlyphShaderProgram compile(ResourceManager resources, Identifier location) throws IOException {
        GlShader vertexShader = compileShader(resources, GlShader.Type.VERTEX, location);
        GlShader fragmentShader = compileShader(resources, GlShader.Type.FRAGMENT, location);

        GlyphShaderProgram program = new GlyphShaderProgram(GlProgramManager.createProgram(), vertexShader, fragmentShader);
        GlProgramManager.linkProgram(program);

        return program;
    }

    private static GlShader compileShader(ResourceManager resources, GlShader.Type type, Identifier location) throws IOException {
        Identifier path = new Identifier(location.getNamespace(), "shaders/" + location.getPath() + type.getFileExtension());
        try (Resource resource = resources.getResource(path)) {
            return GlShader.createFromResource(type, location.toString(), resource.getInputStream(), resource.getResourcePackName());
        }
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
