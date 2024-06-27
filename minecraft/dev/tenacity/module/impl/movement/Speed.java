package dev.tenacity.module.impl.movement;

import dev.tenacity.Tenacity;
import dev.tenacity.event.impl.network.PacketReceiveEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.player.MoveEvent;
import dev.tenacity.event.impl.player.PlayerMoveUpdateEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.combat.TargetStrafe;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.player.MovementUtils;
import dev.tenacity.utils.player.RotationUtils;
import dev.tenacity.utils.server.PacketUtils;
import dev.tenacity.utils.time.TimerUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog",
            "Watchdog", "Strafe", "Vanilla", "BHop", "Verus", "Vulcan", "MMC", "Legit Jump");
    private final ModeSetting watchdogMode = new ModeSetting("Watchdog Mode", "Ground Strafe", "Ground Strafe", "Low Hop");
    private final ModeSetting verusMode = new ModeSetting("Verus Mode", "Normal", "Low", "High-Ish Jump", "Normal");
    private final BooleanSetting autoDisable = new BooleanSetting("Auto Disable", false);
    private final NumberSetting groundSpeed = new NumberSetting("Ground Speed", 2, 5, 1, 0.1);
    private final NumberSetting timer = new NumberSetting("Timer", 1, 5, 1, 0.1);
    private final NumberSetting vanillaSpeed = new NumberSetting("Speed", 1, 10, 1, 0.1);

    private final TimerUtil timerUtil = new TimerUtil();
    private final float r = ThreadLocalRandom.current().nextFloat();
    private double speed, lastDist;
    private float speedChangingDirection;
    private int stage;
    private boolean strafe, wasOnGround;
    private boolean setTimer = true;
    private double moveSpeed;
    private int inAirTicks;

    public Speed() {
        super("Speed", Category.MOVEMENT, "Makes you go faster");
        watchdogMode.addParent(mode, modeSetting -> modeSetting.is("Watchdog"));
        verusMode.addParent(mode, modeSetting -> modeSetting.is("Verus"));
        groundSpeed.addParent(watchdogMode, modeSetting -> modeSetting.is("Ground") && mode.is("Watchdog"));
        vanillaSpeed.addParent(mode, modeSetting -> modeSetting.is("Vanilla") || modeSetting.is("BHop"));
        this.addSettings(mode, vanillaSpeed, watchdogMode, verusMode, autoDisable, groundSpeed, timer);
    }

    @Override
    public void onMotionEvent(MotionEvent e) {
        this.setSuffix(mode.getMode());
        if (setTimer) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        }

        double distX = e.getX() - mc.thePlayer.prevPosX, distZ = e.getZ() - mc.thePlayer.prevPosZ;
        lastDist = Math.hypot(distX, distZ);

        switch (mode.getMode()) {
        
        case "MMC":
        	MovementUtils.strafe();
       	 if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
       	 }
       	  break;
            case "Watchdog":
                switch (watchdogMode.getMode()) {
                    case "Ground Strafe":
                        if (e.isPre()) {
                            if (MovementUtils.isMoving()) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                    MovementUtils.strafe();
                                }
                            }
                        }
                
                        break;
                    case "Low Hop":
                    	  if (e.isPre()) {
                              if (MovementUtils.isMoving()) {
                                  this.mc.timer.timerSpeed = 1.0F;
                                  if (!this.mc.thePlayer.onGround && this.mc.thePlayer.ticksExisted % 10 == 0) {
                                      this.mc.thePlayer.motionY = -0.2;
                                  }

                                  if (this.mc.thePlayer.ticksExisted % 15 == 0) {
                                      this.mc.timer.timerSpeed = 1.0F;
                                  }

                                  if (this.mc.thePlayer.ticksExisted % 25 == 0) {
                                      this.mc.timer.timerSpeed = 1.0F;
                                  }

                                  if (this.mc.thePlayer.ticksExisted % 35 == 0) {
                                      this.mc.timer.timerSpeed = 1.0F;
                                  }

                                  if (this.timer.elasped(600L)) {
                                      MovementUtils.strafe();
                                      this.timer.reset();
                                  }

                                  if (MovementUtils.isMoving() && mc.thePlayer.isOnGround()) {
                                  	MovementUtils.strafe();
                                      this.mc.timer.timerSpeed = 1.0599F;
                                      this.mc.thePlayer.jump();
                                      MovementUtils.setSpeed(0.48);
                                  }
                              }
                          }
                }
                break;
            case "Legit Jump":
            	 if (e.isPre()) {
            	if (mc.thePlayer.onGround) {
                    if (MovementUtils.isMoving()) {
            		 mc.thePlayer.jump();
                    }
            	}
            	 }
                break;
            case "Vulcan":
                if (e.isPre()) {
                    if (MovementUtils.isMoving()) {
                        this.mc.timer.timerSpeed = 1.0F;
                        if (!this.mc.thePlayer.onGround && this.mc.thePlayer.ticksExisted % 10 == 0) {
                            this.mc.thePlayer.motionY = -0.2;
                        }

                        if (this.mc.thePlayer.ticksExisted % 15 == 0) {
                            this.mc.timer.timerSpeed = 1.1F;
                        }

                        if (this.mc.thePlayer.ticksExisted % 25 == 0) {
                            this.mc.timer.timerSpeed = 1.7F;
                        }

                        if (this.mc.thePlayer.ticksExisted % 35 == 0) {
                            this.mc.timer.timerSpeed = 1.7F;
                        }

                        if (this.timer.elasped(600L)) {
                            MovementUtils.strafe();
                            this.timer.reset();
                        }

                        if (MovementUtils.isMoving() && mc.thePlayer.isOnGround()) {
                        	MovementUtils.strafe();
                            this.mc.timer.timerSpeed = 1.0599F;
                            this.mc.thePlayer.jump();
                            MovementUtils.setSpeed(0.48);
                        }
                    }
                }
                break;
                
            case "Vanilla":
                if (MovementUtils.isMoving()) {
                    MovementUtils.setSpeed(vanillaSpeed.getValue() / 4);
                }
                break;
            case "BHop":
                if (MovementUtils.isMoving()) {
                    MovementUtils.setSpeed(vanillaSpeed.getValue() / 4);
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
                break;
            case "Verus":
                switch (verusMode.getMode()) {
                    case "Low":
                        if (e.isPre()) {
                            if (MovementUtils.isMoving()) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                    wasOnGround = true;
                                } else if (wasOnGround) {
                                    if (!mc.thePlayer.isCollidedHorizontally) {
                                        mc.thePlayer.motionY = -0.0784000015258789;
                                    }
                                    wasOnGround = false;
                                }
                                MovementUtils.setSpeed(0.33);
                            } else {
                                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                            }
                        }
                        break;
                    case "Normal":
                        if (e.isPre()) {
                            if(MovementUtils.isMoving()) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                    MovementUtils.setSpeed(0.48);
                                } else {
                                    MovementUtils.setSpeed(MovementUtils.getSpeed());
                                }
                            }
                        }
                        break;
                            case "High-Ish Jump":
            					if(mc.thePlayer.movementInput.moveForward > 0F) {
            						MovementUtils.strafe(0.2);
                            	MovementUtils.strafe(0.37);
                            	if(mc.thePlayer.onGround && MovementUtils.isMoving()) {
                					mc.thePlayer.jump();
                					mc.thePlayer.motionY = 0.5;	
                					}
                        }
                        break;
                }
                break;
            case "Strafe":
                if (e.isPre() && MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    } else {
                        MovementUtils.setSpeed(MovementUtils.getSpeed());
                    }
                }
                break;
        }

    }
    
    public void onPacketReceiveEvent(PacketReceiveEvent e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook && autoDisable.isEnabled()) {
            NotificationManager.post(NotificationType.WARNING, "Flag Detector",
                    "Speed disabled due to " +
                            (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5
                                    ? "world change"
                                    : "lagback"), 1.5F);
            this.toggleSilent();
        }
    }

    public boolean shouldPreventJumping() {
        return Tenacity.INSTANCE.isEnabled(Speed.class) && MovementUtils.isMoving() && !(mode.is("Watchdog") && watchdogMode.is("Ground"));
    }
    

    @Override
    public void onEnable() {
        speed = 1.5f;
        timerUtil.reset();
        if (mc.thePlayer != null) {
            wasOnGround = mc.thePlayer.onGround;
        }
        inAirTicks = 0;
        moveSpeed = 0;
        stage = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

}
