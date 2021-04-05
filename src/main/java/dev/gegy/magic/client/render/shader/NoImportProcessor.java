package dev.gegy.magic.client.render.shader;

import net.minecraft.client.gl.GLImportProcessor;
import org.jetbrains.annotations.Nullable;

public final class NoImportProcessor extends GLImportProcessor {
    public static final NoImportProcessor INSTANCE = new NoImportProcessor();

    private NoImportProcessor() {
    }

    @Nullable
    @Override
    public String loadImport(boolean inline, String name) {
        return null;
    }
}
