package dev.gegy.magic.client.effect.shader;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.render.gl.GlBindableObject;
import dev.gegy.magic.client.render.gl.GlBinding;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

public final class EffectShaderProgram implements Shader, GlBindableObject {
    private static final Binding BINDING = new Binding();

    private final int programRef;
    private final Program vertexShader;
    private final Program fragmentShader;

    private EffectShaderProgram(int programRef, Program vertexShader, Program fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public static EffectShaderProgram compile(ResourceManager resources, ResourceLocation location, VertexFormat format) throws IOException {
        return compile(resources, location, location, format);
    }

    public static EffectShaderProgram compile(ResourceManager resources, ResourceLocation vertexLocation, ResourceLocation fragmentLocation, VertexFormat format) throws IOException {
        Program vertexShader = compileShader(resources, Program.Type.VERTEX, vertexLocation);
        Program fragmentShader = compileShader(resources, Program.Type.FRAGMENT, fragmentLocation);

        int programRef = ProgramManager.createProgram();
        EffectShaderProgram shader = new EffectShaderProgram(programRef, vertexShader, fragmentShader);
        shader.bindAttributes(format);

        ProgramManager.linkShader(shader);

        return shader;
    }

    private static Program compileShader(ResourceManager resources, Program.Type type, ResourceLocation location) throws IOException {
        ResourceLocation path = new ResourceLocation(location.getNamespace(), "shaders/" + location.getPath() + type.getExtension());
        Resource resource = resources.getResourceOrThrow(path);
        try (InputStream input = resource.open()) {
            ImportProcessor importProcessor = new ImportProcessor(location.getNamespace(), resources);
            return Program.compileShader(type, location.toString(), input, resource.sourcePackId(), importProcessor);
        }
    }

    private void bindAttributes(VertexFormat format) {
        int index = 0;
        for (String attribute : format.getElementAttributeNames()) {
            Uniform.glBindAttribLocation(this.programRef, index++, attribute);
        }
    }

    public int getUniformLocation(String name) {
        return Uniform.glGetUniformLocation(this.programRef, name);
    }

    @Override
    public void attachToProgram() {
        this.fragmentShader.attachToShader(this);
        this.vertexShader.attachToShader(this);
    }

    @Override
    public int getId() {
        return this.programRef;
    }

    @Override
    public Program getVertexProgram() {
        return this.vertexShader;
    }

    @Override
    public Program getFragmentProgram() {
        return this.fragmentShader;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public Binding bind() {
        ProgramManager.glUseProgram(this.programRef);
        return BINDING;
    }

    @Override
    public void delete() {
        ProgramManager.releaseProgram(this);
    }

    public static final class Binding implements GlBinding {
        private Binding() {
        }

        @Override
        public void unbind() {
            ProgramManager.glUseProgram(0);
        }
    }

    private static final class ImportProcessor extends GlslPreprocessor {
        private final String namespace;
        private final ResourceProvider resources;

        private final Set<ResourceLocation> visitedImports = new ObjectOpenHashSet<>();

        public ImportProcessor(String namespace, ResourceProvider resources) {
            this.namespace = namespace;
            this.resources = resources;
        }

        @Nullable
        @Override
        public String applyImport(boolean inline, String name) {
            ResourceLocation importLocation = new ResourceLocation(this.namespace, "shaders/include/" + name);
            if (!this.visitedImports.add(importLocation)) {
                return null;
            }

            Optional<Resource> resource = this.resources.getResource(importLocation);
            if (resource.isEmpty()) {
                return "# Missing resource: " + importLocation;
            }

            try (InputStream input = resource.get().open()) {
                return IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Magic.LOGGER.error("Could not open GLSL import {}", name, e);
                return "# Error: " + e.getMessage();
            }
        }
    }
}
