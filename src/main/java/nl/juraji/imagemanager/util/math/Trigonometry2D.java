package nl.juraji.imagemanager.util.math;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public final class Trigonometry2D {

    // Rotational constants
    public static final double DEG_0 = 0.0;
    public static final double DEG_90 = 90.0;
    public static final double DEG_180 = 180.0;
    public static final double DEG_270 = 270.0;
    public static final double DEG_360 = 360.0;

    private Trigonometry2D() {
    }

    /**
     * Rotate a degrees value by an amount
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param currentDeg The value to rotate
     * @param deg        The amount of degrees to rotate.
     *                   Use the result from {@link Trigonometry2D#invertRotation} to rotate counterclockwise
     * @return The resulting degrees
     */
    public static double rotate(double currentDeg, double deg) {
        double targetDeg = currentDeg + deg;
        return absoluteRotation(targetDeg);
    }

    /**
     * Rotate a set of X,Y coordinates around the origin coordinates within a 2D plane
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param deg The amount of degrees to rotate by.
     *            Use the result from {@link Trigonometry2D#invertRotation} to rotate counterclockwise
     * @param x   The original X coordinate
     * @param y   The original Y coordinate
     * @return An array containing the X (0) and Y (1) values
     */
    public static double[] rotateCoordinates(double deg, double x, double y) {
        return rotateCoordinates(deg, x, y, 0, 0);
    }

    /**
     * Rotate a set of X,Y coordinates around a center point (radiusX,radiusY) within a 2D plane
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param deg The amount of degrees to rotate by.
     *            Use the result from {@link Trigonometry2D#invertRotation} to rotate counterclockwise
     * @param x   The original X coordinate
     * @param y   The original Y coordinate
     * @return An array containing the X (0) and Y (1) values
     */
    public static double[] rotateCoordinates(double deg, double x, double y, double radiusX, double radiusY) {
        final double rad = absoluteRotation(deg) * Math.PI / DEG_180;
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double realX = x - radiusX;
        final double realY = y - radiusY;

        final double yi = (realY * cos) - (realX * sin);
        final double xi = (realY * sin) + (realX * cos);

        return new double[]{xi + radiusX, yi + radiusY};
    }

    /**
     * Calculate rectangular corner coordinate offsets
     *
     * @param centerX X coordinate of center
     * @param centerY Y coordinate of center
     * @param width   Box width
     * @param height  Box height
     * @return A set of 4 X,Y coordinates clockwise from top left
     */
    public static double[][] coordinateOffsets(double centerX, double centerY, double width, double height) {
        final double[][] coordinateOffsets = new double[4][2];

        // Top left
        coordinateOffsets[0] = new double[]{centerX, height - centerY};
        // Top right
        coordinateOffsets[1] = new double[]{width - centerX, height - centerY};
        // Bottom right
        coordinateOffsets[2] = new double[]{width - centerX, centerY};
        // Bottom left
        coordinateOffsets[3] = new double[]{centerX, centerY};

        return coordinateOffsets;
    }

    /**
     * Calculate the dimensions for the bounding box of a rotated rectangle
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param rotation The rotation in degrees
     * @param width    The original rectangle width
     * @param height   The original rectangle height
     * @return An array containing the bounding box dimensions [width, height]
     */
    public static double[] getBoundingBox(double rotation, double width, double height) {
        rotation = absoluteRotation(rotation);

        // Save calculations for known perpendicular rotations
        if (rotation == DEG_0 || rotation == DEG_180) {
            return new double[]{width, height};
        } else if (rotation == DEG_90 || rotation == DEG_270) {
            return new double[]{height, width};
        }

        double radiusX = width / 2.0;
        double radiusY = height / 2.0;

        double[] cTL = rotateCoordinates(rotation, 0.0, height, radiusX, radiusY);
        double[] cTR = rotateCoordinates(rotation, width, height, radiusX, radiusY);
        double[] cBL = rotateCoordinates(rotation, 0.0, 0.0, radiusX, radiusY);
        double[] cBR = rotateCoordinates(rotation, width, 0.0, radiusX, radiusY);

        final double maxX = max(cTL[0], cTR[0], cBL[0], cBR[0]).doubleValue();
        final double minX = min(cTL[0], cTR[0], cBL[0], cBR[0]).doubleValue();
        final double maxY = max(cTL[1], cTR[1], cBL[1], cBR[1]).doubleValue();
        final double minY = min(cTL[1], cTR[1], cBL[1], cBR[1]).doubleValue();

        return new double[]{maxX - minX, maxY - minY};
    }

    /**
     * Invert an amount of degrees (i.a.w. rotate value by 180 degrees)
     * (Normalizes large and negative values to 0-359 range)
     *
     * @param deg The amount of degrees to invert
     * @return The inverted amount of degrees
     */
    public static double invertRotation(double deg) {
        return absoluteRotation(deg + DEG_180);
    }

    /**
     * Normalize large and negative values to be contained within a 0-359 degree range
     *
     * @param deg The amount of degrees to normalize
     * @return The normalized amount
     */
    public static double absoluteRotation(double deg) {
        while (deg < DEG_0) {
            deg += DEG_360;
        }

        return deg % DEG_360;
    }

    /**
     * Calculate the greatest value
     *
     * @param numbers Zero or more numbers to compare
     * @return The greatest value
     */
    public static Number max(Number... numbers) {
        double d = 0.0;

        for (Number number : numbers) {
            d = Math.max(d, number.doubleValue());
        }

        return d;
    }

    /**
     * Calculate the lowest value
     *
     * @param numbers Zero or more numbers to compare
     * @return The lowest value
     */
    public static Number min(Number... numbers) {
        double d = Double.MAX_VALUE;

        for (Number number : numbers) {
            d = Math.min(d, number.doubleValue());
        }

        return d;
    }
}
