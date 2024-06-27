package dev.tenacity.ui.clickguis.astolfo;

import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.utils.font.FontUtil;
import dev.tenacity.utils.render.RenderUtil;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class AstolfoNumberButton extends AstolfoButton {
    public NumberSetting setting;
    public Color color;

    public boolean dragged;

    public AstolfoNumberButton(float x, float y, float width, float height, NumberSetting set, Color col) {
        super(x, y, width, height);

        color = col;
        setting = set;
    }

    @Override
    public void drawPanel(int mouseX, int mouseY) {
        double diff = setting.maximum - setting.minimum;

        double percentWidth = (setting.get() - setting.minimum) / (setting.maximum - setting.minimum);

        if (dragged) {
            double val = setting.minimum + (MathHelper.clamp_double((double) (mouseX - x) / width, 0, 1)) * diff;
            setting.setValue(Math.round(val * 100D)/ 100D);
        }

        RenderUtil.drawRect(x, y, x + width, y + height, 0xff181A17);
        RenderUtil.drawRect(x + 3, y, (float) (x + percentWidth*width) - 3, y + height, color.getRGB());
        FontUtil.drawHeightCenteredString(setting.name + ": " + Math.round(setting.get() * 100D)/ 100D, x + 4, y + height / 2 - 0.5f, 0xffffffff);
    }

    @Override
    public void mouseAction(int mouseX, int mouseY, boolean click, int button) {
        if (isHovered(mouseX, mouseY)) {
            dragged = true;
        }

        if(!click) dragged = false;
    }
}
