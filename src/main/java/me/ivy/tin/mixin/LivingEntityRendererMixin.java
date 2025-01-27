package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    // .target removed
    private static final Function<Identifier, RenderLayer> ENTITY_TRANSLUCENT2 = Util.memoize(
            texture -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
//                        .transparency(RenderPhase.OVERLAY_TRANSPARENCY) // Looks cool
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .writeMaskState(RenderPhase.ALL_MASK)
                        .build(true);
                return RenderLayer.of(
                        "entity_translucent2",
                        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                        VertexFormat.DrawMode.QUADS,
                        1536,
                        true,
                        false, // !
                        multiPhaseParameters
                );
            }
    );

    @Inject(at = @At("HEAD"),
            method = "getRenderLayer",
            cancellable = true)
    private void onGetRenderLayer(LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        if (translucent && Tin.getInstance().isEnabled()) {
            Identifier identifier = this.getTexture(state);
            cir.setReturnValue(ENTITY_TRANSLUCENT2.apply(identifier));
        }
    }

    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/ColorHelper;mix(II)I"),
            method = "render")
    private int onMixColors(int color1, int color2) {
        if (color1 != -1 && Tin.getInstance().isEnabled()) {
            color1 = Tin.getRenderHack().getInvisiblePlayerColor();
        }
        return ColorHelper.mix(color1, color2);
    }

    @Inject(at = @At("HEAD"),
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            cancellable = true)
    private void reallyHasLabel(LivingEntity livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        if (Tin.getRenderHack().scaleLabels()) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            cir.setReturnValue(MinecraftClient.isHudEnabled() && livingEntity != minecraftClient.getCameraEntity());
        }
    }

    @Shadow
    public abstract Identifier getTexture(LivingEntityRenderState state);
}
