package dev.tenacity.module.impl.player;

import dev.tenacity.event.EventListener;
import dev.tenacity.event.IEventListener;
import dev.tenacity.event.impl.network.PacketReceiveEvent;
import dev.tenacity.event.impl.network.PacketSendEvent;
import dev.tenacity.event.impl.player.BoundingBoxEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.player.PreMotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.player.MovementUtils;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import dev.tenacity.utils.server.PacketUtils;

@SuppressWarnings("unused")
public final class NoFall extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Packet", "No Ground", "Verus");
    private double dist;
    private boolean doNofall;
    private double lastFallDistance;
    private boolean c04;

    public NoFall() {
        super("NoFall", Category.PLAYER, "prevents fall damage");
        this.addSettings(mode);
    }

    @Override
    public void onMotionEvent(MotionEvent event) {
        if (event.isPre()) {
            this.setSuffix(mode.getMode());
            if (mc.thePlayer.fallDistance > 3.0 && isBlockUnder()) {
                switch (mode.getMode()) {
                    case "Vanilla":
                        event.setOnGround(true);
                        break;
                    case "Packet":
                        PacketUtils.sendPacket(new C03PacketPlayer(true));
                        break;
                }
                mc.thePlayer.fallDistance = 0;
            }
        }
    }


    public final IEventListener<PreMotionEvent> onPreMotionEvent = e -> {
        e.setOnGround(false);
        e.setPosY(e.getPosY() + Math.random() / 100000000000000000000f);
    };
    
    @Override
    public void onBoundingBoxEvent(BoundingBoxEvent event) {
        if(mode.is("Verus") && mc.thePlayer.fallDistance > 2) {
            final AxisAlignedBB axisAlignedBB = AxisAlignedBB.fromBounds(-5, -1, -5, 5, 1, 5).offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ());
            event.setBoundingBox(axisAlignedBB);
        }
    }

    private boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0) return false;
        for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
