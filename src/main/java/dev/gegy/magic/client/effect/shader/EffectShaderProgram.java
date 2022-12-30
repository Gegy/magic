package dev.gegy.magic.client.effect.shader;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.gl.GlBindableObject;
import dev.gegy.magic.client.render.gl.GlBinding;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgramSetupView;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

public final class EffectShaderProgram implements ShaderProgramSetupView, GlBindableObject {
    private static final Binding BINDING = new Binding();

    private final int programRef;
    private final ShaderStage vertexShader;
    private final ShaderStage fragmentShader;

    private EffectShaderProgram(int programRef, ShaderStage vertexShader, ShaderStage fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public static EffectShaderProgram compile(ResourceManager resources, Identifier location, VertexFormat format) throws IOException {
        return compile(resources, location, location, format);
    }

    public static EffectShaderProgram compile(ResourceManager resources, Identifier vertexLocation, Identifier fragmentLocation, VertexFormat format) throws IOException {
        ShaderStage vertexShader = compileShader(resources, ShaderStage.Type.VERTEX, vertexLocation);
        ShaderStage fragmentShader = compileShader(resources, ShaderStage.Type.FRAGMENT, fragmentLocation);

        int programRef = GlProgramManager.createProgram();
        EffectShaderProgram shader = new EffectShaderProgram(programRef, vertexShader, fragmentShader);
        shader.bindAttributes(format);

        GlProgramManager.linkProgram(shader);

        return shader;
    }

    private static ShaderStage compileShader(ResourceManager resources, ShaderStage.Type type, Identifier location) throws IOException {
        Identifier path = new Identifier(location.getNamespace(), "shaders/" + location.getPath() + type.getFileExtension());
        Resource resource = resources.getResourceOrThrow(path);
        try (InputStream input = resource.getInputStream()) {
            ImportProcessor importProcessor = new ImportProcessor(location.getNamespace(), resources);
            return ShaderStage.createFromResource(type, location.toString(), input, resource.getResourcePackName(), importProcessor);
        }
    }

    private void bindAttributes(VertexFormat format) {
        int index = 0;
        for (String attribute : format.getAttributeNames()) {
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
    public int getGlRef() {
        return this.programRef;
    }

    @Override
    public ShaderStage getVertexShader() {
        return this.vertexShader;
    }

    @Override
    public ShaderStage getFragmentShader() {
        return this.fragmentShader;
    }

    @Override
    public void markUniformsDirty() {
    }

    @Override
    public Binding bind() {
        GlProgramManager.useProgram(this.programRef);
        return BINDING;
    }

    @Override
    public void delete() {
        GlProgramManager.deleteProgram(this);
    }

    public static final class Binding implements GlBinding {
        private Binding() {
        }

        @Override
        public void unbind() {
            GlProgramManager.useProgram(0);
        }
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

            Optional<Resource> resource = this.resources.getResource(importLocation);
            if (resource.isEmpty()) {
                return "# Missing resource: " + importLocation;
            }

            try (InputStream input = resource.get().getInputStream()) {
                return IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Magic.LOGGER.error("Could not open GLSL import {}", name, e);
                return "# Error: " + e.getMessage();
            }
        }
    }
}
