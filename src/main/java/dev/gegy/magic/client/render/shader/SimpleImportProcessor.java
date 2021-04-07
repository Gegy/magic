package dev.gegy.magic.client.render.shader;

import dev.gegy.magic.Magic;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class SimpleImportProcessor extends GLImportProcessor {
    private final String namespace;
    private final ResourceFactory resources;

    private final Set<Identifier> visitedImports = new ObjectOpenHashSet<>();

    public SimpleImportProcessor(String namespace, ResourceFactory resources) {
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
