package dev.tenacity.module.impl.player;

import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.player.SlowDownEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.player.MovementUtils;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Vanilla", "NCP", "Watchdog");
    private boolean synced;

    public NoSlow() {
        super("NoSlow", Category.PLAYER, "prevent item slowdown");
        this.addSettings(mode);
    }

    @Override
    public void onSlowDownEvent(SlowDownEvent event) {
        event.cancel();
    }

    @Override
    public void onMotionEvent(MotionEvent e) {
        this.setSuffix(mode.getMode());
        switch (mode.getMode()) {
            case "Watchdog":
            	if(mc.thePlayer.isUsingItem() && mc.thePlayer.itemInUse != null && mc.thePlayer.itemInUse.item != null) {
            		mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
                break;
            case "NCP":
                if (MovementUtils.isMoving() && mc.thePlayer.isUsingItem()) {
                    if (e.isPre()) {
                        PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    } else {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                    }

                }
                break;
        }
    }

}
