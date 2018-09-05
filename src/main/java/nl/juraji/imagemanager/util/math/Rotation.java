package nl.juraji.imagemanager.util.math;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public final class Rotation {
    public static final double QUARTER_CIRCLE = 90.0;
    public static final double HALF_CIRCLE = 180.0;
    public static final double FULL_CIRCLE = 360.0;

    private Rotation() {
    }

    /**
     * Rotate a degrees value by an amount
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param currentDeg The value to rotate
     * @param deg        The amount of degrees to rotate.
     *                   Use the result from {@link Rotation#invert} to rotate counterclockwise
     * @return The resulting degrees
     */
    public static double rotate(double currentDeg, double deg) {
        double targetDeg = currentDeg + deg;
        return absoluteDeg(targetDeg);
    }

    /**
     * Rotate a set of X,Y coordinates around their origin within a 2D plane
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param deg The amount of degrees to rotate by.
     *            Use the result from {@link Rotation#invert} to rotate counterclockwise
     * @param x   The original X coordinate
     * @param y   The original Y coordinate
     * @return An array containing the X (0) and Y (1) values
     */
    public static double[] rotateCoordinates(double deg, double x, double y) {
        final double rad = absoluteDeg(deg) * Math.PI / HALF_CIRCLE;
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);

        final double yi = (y * cos) - (x * sin);
        final double xi = (y * sin) + (x * cos);

        return new double[]{xi, yi};
    }

    /**
     * Invert an amount of degrees (i.a.w. rotate value by 180 degrees)
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param deg The amount of degrees to invert
     * @return The inverted amount of degrees
     */
    public static double invert(double deg) {
        return absoluteDeg(deg + HALF_CIRCLE);
    }

    /**
     * Normalize large and negative values to be contained with the 0-359 degree range
     *
     * @param deg The amount of degrees to normalize
     * @return The normalized amount
     */
    public static double absoluteDeg(double deg) {
        while (deg < 0) {
            deg += FULL_CIRCLE;
        }

        return deg % FULL_CIRCLE;
    }
}
