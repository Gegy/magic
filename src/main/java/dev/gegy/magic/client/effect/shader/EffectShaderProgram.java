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

    private EffectShaderProgram(final int programRef, final Program vertexShader, final Program fragmentShader) {
        this.programRef = programRef;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public static EffectShaderProgram compile(final ResourceManager resources, final ResourceLocation location, final VertexFormat format) throws IOException {
        return compile(resources, location, location, format);
    }

    public static EffectShaderProgram compile(final ResourceManager resources, final ResourceLocation vertexLocation, final ResourceLocation fragmentLocation, final VertexFormat format) throws IOException {
        final Program vertexShader = compileShader(resources, Program.Type.VERTEX, vertexLocation);
        final Program fragmentShader = compileShader(resources, Program.Type.FRAGMENT, fragmentLocation);

        final int programRef = ProgramManager.createProgram();
        final EffectShaderProgram shader = new EffectShaderProgram(programRef, vertexShader, fragmentShader);
        shader.bindAttributes(format);

        ProgramManager.linkShader(shader);

        return shader;
    }

    private static Program compileShader(final ResourceManager resources, final Program.Type type, final ResourceLocation location) throws IOException {
        final ResourceLocation path = new ResourceLocation(location.getNamespace(), "shaders/" + location.getPath() + type.getExtension());
        final Resource resource = resources.getResourceOrThrow(path);
        try (final InputStream input = resource.open()) {
            final ImportProcessor importProcessor = new ImportProcessor(location.getNamespace(), resources);
            return Program.compileShader(type, location.toString(), input, resource.sourcePackId(), importProcessor);
        }
    }

    private void bindAttributes(final VertexFormat format) {
        int index = 0;
        for (final String attribute : format.getElementAttributeNames()) {
            Uniform.glBindAttribLocation(programRef, index++, attribute);
        }
    }

    public int getUniformLocation(final String name) {
        return Uniform.glGetUniformLocation(programRef, name);
    }

    @Override
    public void attachToProgram() {
        fragmentShader.attachToShader(this);
        vertexShader.attachToShader(this);
    }

    @Override
    public int getId() {
        return programRef;
    }

    @Override
    public Program getVertexProgram() {
        return vertexShader;
    }

    @Override
    public Program getFragmentProgram() {
        return fragmentShader;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public Binding bind() {
        ProgramManager.glUseProgram(programRef);
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

        public ImportProcessor(final String namespace, final ResourceProvider resources) {
            this.namespace = namespace;
            this.resources = resources;
        }

        @Nullable
        @Override
        public String applyImport(final boolean inline, final String name) {
            final ResourceLocation importLocation = new ResourceLocation(namespace, "shaders/include/" + name);
            if (!visitedImports.add(importLocation)) {
                return null;
            }

            final Optional<Resource> resource = resources.getResource(importLocation);
            if (resource.isEmpty()) {
                return "# Missing resource: " + importLocation;
            }

            try (final InputStream input = resource.get().open()) {
                return IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                Magic.LOGGER.error("Could not open GLSL import {}", name, e);
                return "# Error: " + e.getMessage();
            }
        }
    }
}
