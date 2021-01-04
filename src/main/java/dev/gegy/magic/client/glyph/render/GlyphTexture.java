package dev.gegy.magic.client.glyph.render;

import com.mojang.blaze3d.platform.FramebufferInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

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
        int framebufferRef = GlStateManager.genFramebuffers();
        int textureRef = TextureUtil.generateId();

        GlStateManager.bindTexture(textureRef);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, SIZE, SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        GlStateManager.bindFramebuffer(FramebufferInfo.FRAME_BUFFER, framebufferRef);
        GlStateManager.framebufferTexture2D(FramebufferInfo.FRAME_BUFFER, FramebufferInfo.COLOR_ATTACHMENT, GL11.GL_TEXTURE_2D, textureRef, 0);

        GlStateManager.bindTexture(0);

        return new GlyphTexture(framebufferRef, textureRef);
    }

    public void bindWrite() {
        GlStateManager.bindFramebuffer(FramebufferInfo.FRAME_BUFFER, this.framebufferRef);
        GlStateManager.viewport(0, 0, SIZE, SIZE);
    }

    public void unbindWrite() {
        GlStateManager.bindFramebuffer(FramebufferInfo.FRAME_BUFFER, 0);
    }

    public void bindRead() {
        GlStateManager.bindTexture(this.textureRef);
    }

    public void unbindRead() {
        GlStateManager.bindTexture(0);
    }

    @Override
    public void close() {
        TextureUtil.deleteId(this.textureRef);
        GlStateManager.deleteFramebuffers(this.framebufferRef);
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
