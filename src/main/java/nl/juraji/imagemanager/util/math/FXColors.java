package nl.juraji.imagemanager.util.math;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * Created by Juraji on 9-9-2018.
 * Image Manager
 */
public final class FXColors {
    private FXColors() {
    }

    public static ColorAdjust colorAdjustEffect(Color targetColor) {
        final ColorAdjust colorAdjust = new ColorAdjust();

        double hue = getHue(targetColor);
        colorAdjust.setHue(hue);
        colorAdjust.setSaturation(targetColor.getSaturation());
        colorAdjust.setBrightness(targetColor.getBrightness());

        return colorAdjust;
    }

    public static ColorAdjust lightenEffect(double amount) {
        final ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(amount);
        return colorAdjust;
    }

    public static double getHue(Color color) {
        return ((color.getHue() + 180.0) % 360.0) / -2;
    }

    public static BackgroundFill copyFillToColor(BackgroundFill fill, int red, int green, int blue, double alpha) {
        return new BackgroundFill(
                Color.rgb(red, green, blue, alpha),
                fill.getRadii(),
                fill.getInsets()
        );
    }
}
