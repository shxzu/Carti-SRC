package dev.tenacity.module.impl.combat;

import dev.tenacity.event.impl.game.WorldEvent;
import dev.tenacity.event.impl.network.PacketReceiveEvent;
import dev.tenacity.event.impl.network.PacketSendEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.player.MovementUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;

public class Velocity extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Watchdog", "Custom", "Vulcan Dev", "Jump");
    private final ModeSetting watchdogMode = new ModeSetting("Watchdog Mode", "0,0 Mitigate", "0,0 Mitigate", "0,100 EZ");
    private final NumberSetting horizontal = new NumberSetting("Horizontal", 0, 100, 0, 1);
    private final NumberSetting vertical = new NumberSetting("Vertical", 0, 100, 0, 1);
    private final NumberSetting chance = new NumberSetting("Chance", 100, 100, 0, 1);
    private final BooleanSetting onlyWhileMoving = new BooleanSetting("Only while moving", false);
    private final BooleanSetting staffCheck = new BooleanSetting("Staff check", false);

    private long lastDamageTimestamp, lastAlertTimestamp;
    private boolean cancel;
    private int stack;

    public Velocity() {
        super("Velocity", Category.COMBAT, "Reduces your knockback");
        Setting.addParent(mode, m -> m.is("Custom"), horizontal, vertical, staffCheck);
        watchdogMode.addParent(mode, modeSetting -> modeSetting.is("Watchdog"));
        this.addSettings(mode, horizontal, vertical, watchdogMode, chance, onlyWhileMoving, staffCheck);
    }

    @Override
    public void onPacketReceiveEvent(PacketReceiveEvent e) {
        this.setSuffix(mode.getMode());
        if ((onlyWhileMoving.isEnabled() && !MovementUtils.isMoving()) || (chance.getValue() != 100 && MathUtils.getRandomInRange(0, 100) > chance.getValue()))
            return;
        Packet<?> packet = e.getPacket();
        switch (mode.getMode()) {   
            case "Custom":
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        if (cancel(e)) return;
                        s12.motionX *= horizontal.getValue() / 100.0;
                        s12.motionZ *= horizontal.getValue() / 100.0;
                        s12.motionY *= vertical.getValue() / 100.0;
                    }
                } else if (packet instanceof S27PacketExplosion) {
                    if (cancel(e)) return;
                    S27PacketExplosion s27 = (S27PacketExplosion) e.getPacket();
                    s27.motionX *= horizontal.getValue() / 100.0;
                    s27.motionZ *= horizontal.getValue() / 100.0;
                    s27.motionY *= vertical.getValue() / 100.0;
                } else if (e.getPacket() instanceof S19PacketEntityStatus) {
                    S19PacketEntityStatus s19 = (S19PacketEntityStatus) e.getPacket();
                    if (mc.thePlayer != null && s19.getEntityId() == mc.thePlayer.getEntityId() && s19.getOpCode() == 2) {
                        lastDamageTimestamp = System.currentTimeMillis();
                    }
                }
                break;

            case "Jump":
            	  if (mc.thePlayer.hurtTime >= 8) {
                      mc.gameSettings.keyBindJump.pressed = true;
                  }
                  if (mc.thePlayer.hurtTime >= 7) {
                      mc.gameSettings.keyBindForward.pressed = true;
                  } else if (mc.thePlayer.hurtTime >= 4) {
                      mc.gameSettings.keyBindJump.pressed = false;
                      mc.gameSettings.keyBindForward.pressed = false;
                  } else if (mc.thePlayer.hurtTime > 1) {
                      mc.gameSettings.keyBindForward.pressed = mc.gameSettings.isKeyDown(mc.gameSettings.keyBindForward);
                      mc.gameSettings.keyBindJump.pressed = mc.gameSettings.isKeyDown(mc.gameSettings.keyBindJump);
                  }
    			break;

                
            case "Watchdog":
            	switch (watchdogMode.getMode()) {
            	case "0,0 Mitigate":
            		 if (packet instanceof S12PacketEntityVelocity) {
                         S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                         if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                             if (cancel(e)) return;
            		s12.motionX *= 0 / 100.0;
                    s12.motionZ *= 0 / 100.0;
                    s12.motionY *= 0 / 100.0;
                    break;
            	}
            		 }
            	case "0,100 EZ":
            		 if (packet instanceof S12PacketEntityVelocity) {
                         S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
            		s12.motionX *= 0 / 100.0;
                    s12.motionZ *= 0 / 100.0;
                    s12.motionY *= 100 / 100.0;
                    break;
            	}
            		 }
                
            case "Vulcan Dev":
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) packet;
                    cancel = !cancel;
                    if (cancel) {
                        e.cancel();
                    }
                }
                if (packet instanceof S27PacketExplosion) {
                    e.cancel();
                }
                break;
        }
    }

    @Override
    public void onWorldEvent(WorldEvent event) {
        stack = 0;
    }

    private boolean cancel(PacketReceiveEvent e) {
        if (staffCheck.isEnabled() && System.currentTimeMillis() - lastDamageTimestamp > 500) {
            if (System.currentTimeMillis() - lastAlertTimestamp > 250) {
                NotificationManager.post(NotificationType.WARNING, "Velocity", "Suspicious knockback detected!", 2);
                lastAlertTimestamp = System.currentTimeMillis();
            }
            return true;
        }
        if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
            e.cancel();
            return true;
        }
        return false;
    }

}
