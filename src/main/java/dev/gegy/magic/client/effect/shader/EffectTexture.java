package dev.gegy.magic.client.effect.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.render.gl.GlBinding;
import dev.gegy.magic.client.render.gl.GlGeometry;
import dev.gegy.magic.client.render.gl.GlObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class EffectTexture<T> implements GlObject {
    private static final ReadBinding READ_BINDING = new ReadBinding();
    private static final WriteBinding WRITE_BINDING = new WriteBinding();

    private final EffectShader<T> shader;

    private final int width;
    private final int height;
    private final int framebufferRef;
    private final int textureRef;

    private EffectTexture(EffectShader<T> shader, int width, int height, int framebufferRef, int textureRef) {
        this.shader = shader;
        this.width = width;
        this.height = height;
        this.framebufferRef = framebufferRef;
        this.textureRef = textureRef;
    }

    public static <T> EffectTexture<T> create(EffectShader<T> shader, int size) {
        return create(shader, size, size);
    }

    public static <T> EffectTexture<T> create(EffectShader<T> shader, int width, int height) {
        int framebufferRef = GlStateManager.glGenFramebuffers();
        int textureRef = TextureUtil.generateTextureId();

        GlStateManager._bindTexture(textureRef);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferRef);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureRef, 0);

        GlStateManager._bindTexture(0);

        return new EffectTexture<>(shader, width, height, framebufferRef, textureRef);
    }

    public void renderWith(T parameters, GlGeometry.Binding geometryBinding) {
        try (
                var shaderBinding = this.shader.bind(parameters);
                var writeBinding = this.bindWrite()
        ) {
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);

            geometryBinding.draw();
        }
    }

    public ReadBinding bindRead() {
        GlStateManager._bindTexture(this.textureRef);
        return READ_BINDING;
    }

    private WriteBinding bindWrite() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebufferRef);
        RenderSystem.viewport(0, 0, this.width, this.height);
        return WRITE_BINDING;
    }

    @Override
    public void delete() {
        TextureUtil.releaseTextureId(this.textureRef);
        GlStateManager._glDeleteFramebuffers(this.framebufferRef);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public static final class ReadBinding implements GlBinding {
        private ReadBinding() {
        }

        @Override
        public void unbind() {
            GlStateManager._bindTexture(0);
        }
    }

    public static final class WriteBinding implements GlBinding {
        private WriteBinding() {
        }

        @Override
        public void unbind() {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }
}
