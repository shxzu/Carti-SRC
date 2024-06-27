package dev.tenacity.module.impl.combat;

import dev.tenacity.event.Event;
import dev.tenacity.event.IEventListener;
import dev.tenacity.event.impl.network.PacketReceiveEvent;
import dev.tenacity.event.impl.player.KeepSprintEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.utils.misc.TimeUtils;
import dev.tenacity.utils.time.TimerUtil;

public final class AutoClicker extends Module {
	
	private final TimerUtil clickTimer = new TimerUtil();
	
	 private final NumberSetting maxCPS = new NumberSetting("Max CPS", 1, 20, 1, 2);
	    private final NumberSetting minCPS = new NumberSetting("Min. CPS", 1, 20, 1, 2);

    public AutoClicker() {
        super("AutoClicker", Category.COMBAT, "Automatically clicks for you");
        this.addSettings(maxCPS, minCPS);
    }

    private final IEventListener<MotionEvent> onMotion = event -> {
        if(event.isPost()) return;
        
    	  final int maxValue = (int) ((minCPS.getMaxValue() - maxCPS.getValue()) * 20);
          final int minValue = (int) ((minCPS.getMaxValue() - minCPS.getValue()) * 20);
          long cps = TimeUtils.randomDelay(minValue, maxValue);

                  if(!mc.gameSettings.keyBindAttack.isKeyDown()) {
                      clickTimer.reset();
                      return;
                  }
                  if(clickTimer.hasTimeElapsed(cps, true)) {
                      if(mc.gameSettings.keyBindUseItem.isKeyDown()) return;
                      mc.leftClickCounter = 0;
                      mc.clickMouse();
                  }
                  if(clickTimer.hasTimeElapsed(cps -2, true)) {
                      if(mc.gameSettings.keyBindUseItem.isKeyDown()) return;
                      mc.leftClickCounter = 0;
                      mc.clickMouse();
                  }
              };
    }
