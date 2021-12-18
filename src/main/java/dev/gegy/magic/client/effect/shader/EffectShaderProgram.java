package dev.gegy.magic.client.effect.shader;

import dev.gegy.magic.Magic;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class EffectShaderProgram implements GlShader, AutoCloseable {
    private final int programRef;
    private final Program vertexShader;
    private final Program fragmentShader;

    private EffectShaderProgram(int programRef, Program vertexShader, Program fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public static EffectShaderProgram compile(ResourceManager resources, Identifier location, VertexFormat format) throws IOException {
        return compile(resources, location, location, format);
    }

    public static EffectShaderProgram compile(ResourceManager resources, Identifier vertexLocation, Identifier fragmentLocation, VertexFormat format) throws IOException {
        Program vertexShader = compileShader(resources, Program.Type.VERTEX, vertexLocation);
        Program fragmentShader = compileShader(resources, Program.Type.FRAGMENT, fragmentLocation);

        int programRef = GlProgramManager.createProgram();
        EffectShaderProgram shader = new EffectShaderProgram(programRef, vertexShader, fragmentShader);
        shader.bindAttributes(format);

        GlProgramManager.linkProgram(shader);

        return shader;
    }

    private static Program compileShader(ResourceManager resources, Program.Type type, Identifier location) throws IOException {
        Identifier path = new Identifier(location.getNamespace(), "shaders/" + location.getPath() + type.getFileExtension());
        try (Resource resource = resources.getResource(path)) {
            ImportProcessor importProcessor = new ImportProcessor(location.getNamespace(), resources);
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

    private static final class ImportProcessor extends GLImportProcessor {
        private final String namespace;
        private final ResourceFactory resources;

        private final Set<Identifier> visitedImports = new ObjectOpenHashSet<>();

        public ImportProcessor(String namespace, ResourceFactory resources) {
            this.namespace = namespace;
            this.resources = resources;
        }

        @Nullable
        @Override
        public String loadImport(boolean inline, String name) {
            Identifier importLocation = new Identifier(this.namespace, "shaders/include/" + name);
            if (!this.visitedImports.add(importLocation)) {
                return null;
            }

            try (Resource resource = this.resources.getResource(importLocation)) {
                return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                Magic.LOGGER.error("Could not open GLSL import {}", name, e);
                return "#error " + e.getMessage();
            }
        }
    }
}
