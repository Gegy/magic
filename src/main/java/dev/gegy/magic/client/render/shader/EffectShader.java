package dev.gegy.magic.client.render.shader;

import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

public final class EffectShader implements GlShader, AutoCloseable {
    final int programRef;
    final Program vertexShader;
    final Program fragmentShader;
    final VertexFormat format;

    private EffectShader(int programRef, Program vertexShader, Program fragmentShader, VertexFormat format) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.format = format;
    }

    public static EffectShader compile(ResourceManager resources, Identifier location, VertexFormat format) throws IOException {
        return compile(resources, location, location, format);
    }

    public static EffectShader compile(ResourceManager resources, Identifier vertexLocation, Identifier fragmentLocation, VertexFormat format) throws IOException {
        Program vertexShader = compileShader(resources, Program.Type.VERTEX, vertexLocation);
        Program fragmentShader = compileShader(resources, Program.Type.FRAGMENT, fragmentLocation);

        int programRef = GlProgramManager.createProgram();
        EffectShader shader = new EffectShader(programRef, vertexShader, fragmentShader, format);
        shader.bindAttributes(format);

        GlProgramManager.linkProgram(shader);

        return shader;
    }

    private static Program compileShader(ResourceManager resources, Program.Type type, Identifier location) throws IOException {
        Identifier path = new Identifier(location.getNamespace(), "shaders/" + location.getPath() + type.getFileExtension());
        try (Resource resource = resources.getResource(path)) {
            SimpleImportProcessor importProcessor = new SimpleImportProcessor(location.getNamespace(), resources);
            return Program.createFromResource(type, location.toString(), resource.getInputStream(), resource.getResourcePackName(), importProcessor);
        }
    }

    private void bindAttributes(VertexFormat format) {
        int index = 0;
        for (String attribute : format.getShaderAttributes()) {
            GlUniform.bindAttribLocation(this.programRef, index++, attribute);
        }
    }

    public int getUniformLocation(String name) {
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

    public void bind() {
        GlProgramManager.useProgram(this.programRef);
    }

    public void unbind() {
        GlProgramManager.useProgram(0);
    }
}
