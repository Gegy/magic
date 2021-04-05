package dev.gegy.magic.client.glyph.render.shader;

import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

final class GlyphShaderProgram implements GlShader, AutoCloseable {
    final int programRef;
    final Program vertexShader;
    final Program fragmentShader;

    private GlyphShaderProgram(int programRef, Program vertexShader, Program fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    static GlyphShaderProgram compile(ResourceManager resources, Identifier location) throws IOException {
        Program vertexShader = compileShader(resources, Program.Type.VERTEX, location);
        Program fragmentShader = compileShader(resources, Program.Type.FRAGMENT, location);

        GlyphShaderProgram program = new GlyphShaderProgram(GlProgramManager.createProgram(), vertexShader, fragmentShader);
        GlProgramManager.linkProgram(program);

        return program;
    }

    private static Program compileShader(ResourceManager resources, Program.Type type, Identifier location) throws IOException {
        Identifier path = new Identifier(location.getNamespace(), "shaders/" + location.getPath() + type.getFileExtension());
        try (Resource resource = resources.getResource(path)) {
            return Program.createFromResource(type, location.toString(), resource.getInputStream(), resource.getResourcePackName(), NoImportProcessor.INSTANCE);
        }
    }

    int getUniformLocation(String name) {
        return GlUniform.getUniformLocation(this.programRef, name);
    }

    @Override
    public void attachReferencedShaders() {
        this.fragmentShader.attachTo(this);
        this.vertexShader.attachTo(this);
    }

    @Override
    public int getProgramRef() {
        return this.programRef;
    }

    @Override
    public Program getVertexShader() {
        return this.vertexShader;
    }

    @Override
    public Program getFragmentShader() {
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
