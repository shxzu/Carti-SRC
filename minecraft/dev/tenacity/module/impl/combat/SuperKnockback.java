package dev.tenacity.module.impl.combat;

import dev.tenacity.event.impl.player.AttackEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public final class SuperKnockback extends Module {
	
	  private final ModeSetting kbMode = new ModeSetting("KB Mode", "Legit", "Packet", "Legit");
    
	  public SuperKnockback() {
        super("SuperKnockback", Category.COMBAT, "Makes the player your attacking take extra knockback");
        this.addSettings(kbMode);
    }

    @Override
    public void onAttackEvent(AttackEvent event) {
    	switch (kbMode.getMode()) {
    	case "Legit":
    		if (mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(false);
                
                mc.thePlayer.setSprinting(true);
                mc.thePlayer.setSprinting(false);
                mc.thePlayer.setSprinting(true);
    		}
    		break;
    case "Packet":
        if(event.getTargetEntity() != null) {
            if (mc.thePlayer.isSprinting())
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));

            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        break;
        		}
    		}
    }
}