package dev.gegy.magic.client.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class EffectTexture<T> implements AutoCloseable {
    private final RenderEffect<T> effect;

    private final int width;
    private final int height;
    private final int framebufferRef;
    private final int textureRef;

    private EffectTexture(RenderEffect<T> effect, int width, int height, int framebufferRef, int textureRef) {
        this.effect = effect;
        this.width = width;
        this.height = height;
        this.framebufferRef = framebufferRef;
        this.textureRef = textureRef;
    }

    public static <T> EffectTexture<T> create(RenderEffect<T> shader, int size) {
        return create(shader, size, size);
    }

    public static <T> EffectTexture<T> create(RenderEffect<T> shader, int width, int height) {
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

    public void renderWith(T data, VertexBuffer geometry) {
        this.effect.bind(data);

        this.bindWrite();
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);

        geometry.method_35665();

        this.unbindWrite();

        this.effect.unbind();
    }

    private void bindWrite() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebufferRef);
        RenderSystem.viewport(0, 0, this.width, this.height);
    }

    private void unbindWrite() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void bindRead() {
        GlStateManager._bindTexture(this.textureRef);
    }

    public void unbindRead() {
        GlStateManager._bindTexture(0);
    }

    @Override
    public void close() {
        TextureUtil.releaseTextureId(this.textureRef);
        GlStateManager._glDeleteFramebuffers(this.framebufferRef);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
