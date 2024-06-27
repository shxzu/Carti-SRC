package dev.tenacity.event.impl.player;

import dev.tenacity.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public final class PreMotionEvent extends Event{
	    private double posX;
	    private double posY;
	    private double posZ;
	    private float yaw;
	    private float pitch;
	    private boolean onGround;
	}

