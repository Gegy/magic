package dev.gegy.magic.client.glyph.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class GlyphTexture implements AutoCloseable {
    private static final int SIZE = 64;
    private static final float RENDER_SIZE = 32.0F;

    private static final float TEXEL_SIZE = 1.0F / SIZE;
    private static final float RENDER_SCALE = SIZE / RENDER_SIZE;

    private final int framebufferRef;
    private final int textureRef;

    GlyphTexture(int framebufferRef, int textureRef) {
        this.framebufferRef = framebufferRef;
        this.textureRef = textureRef;
    }

    static GlyphTexture create() {
        int framebufferRef = GlStateManager.glGenFramebuffers();
        int textureRef = TextureUtil.generateTextureId();

        GlStateManager._bindTexture(textureRef);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, SIZE, SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferRef);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureRef, 0);

        GlStateManager._bindTexture(0);

        return new GlyphTexture(framebufferRef, textureRef);
    }

    public void bindWrite() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebufferRef);
        RenderSystem.viewport(0, 0, SIZE, SIZE);
    }

    public void unbindWrite() {
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

    public int getSize() {
        return SIZE;
    }

    public float getTexelSize() {
        return TEXEL_SIZE;
    }

    public float getRenderSize() {
        return RENDER_SIZE;
    }

    public float getRenderScale() {
        return RENDER_SCALE;
    }
}
