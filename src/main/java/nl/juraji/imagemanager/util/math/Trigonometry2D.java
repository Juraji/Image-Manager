package nl.juraji.imagemanager.util.math;

import java.util.Arrays;

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
    public static BoundingBox rotateCoordinates(double deg, double x, double y) {
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
    public static BoundingBox rotateCoordinates(double deg, double x, double y, double radiusX, double radiusY) {
        final double rad = absoluteRotation(deg) * Math.PI / DEG_180;
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double realX = x - radiusX;
        final double realY = y - radiusY;

        final double yi = (realY * cos) - (realX * sin);
        final double xi = (realY * sin) + (realX * cos);

        return new BoundingBox(xi + radiusX, yi + radiusY);
    }

    /**
     * Calculate rectangular corner coordinate offsets
     *
     * @param axisX  X position of X axis
     * @param axisY  Y position of Y axis
     * @param width  Box width
     * @param height Box height
     * @return A set of 4 X,Y coordinates clockwise from top left
     */
    public static CoordinateOffsets coordinateOffsets(double axisX, double axisY, double width, double height) {
        return new CoordinateOffsets(axisX, axisY, width, height);
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
    public static BoundingBox getBoundingBox(double rotation, double width, double height) {
        rotation = absoluteRotation(rotation);

        // Save calculations for known perpendicular rotations
        if (rotation == DEG_0 || rotation == DEG_180) {
            return new BoundingBox(width, height);
        } else if (rotation == DEG_90 || rotation == DEG_270) {
            //noinspection SuspiciousNameCombination It's supposed to be backwards
            return new BoundingBox(height, width);
        }

        double radiusX = width / 2.0;
        double radiusY = height / 2.0;

        BoundingBox cTL = rotateCoordinates(rotation, 0.0, height, radiusX, radiusY);
        BoundingBox cTR = rotateCoordinates(rotation, width, height, radiusX, radiusY);
        BoundingBox cBL = rotateCoordinates(rotation, 0.0, 0.0, radiusX, radiusY);
        BoundingBox cBR = rotateCoordinates(rotation, width, 0.0, radiusX, radiusY);

        final double maxX = max(cTL.getX(), cTR.getX(), cBL.getX(), cBR.getX()).doubleValue();
        final double minX = min(cTL.getX(), cTR.getX(), cBL.getX(), cBR.getX()).doubleValue();
        final double maxY = max(cTL.getY(), cTR.getY(), cBL.getY(), cBR.getY()).doubleValue();
        final double minY = min(cTL.getY(), cTR.getY(), cBL.getY(), cBR.getY()).doubleValue();

        return new BoundingBox(maxX - minX, maxY - minY);
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
        return Arrays.stream(numbers)
                .mapToDouble(Number::doubleValue)
                .max()
                .orElse(Double.MAX_VALUE);
    }

    /**
     * Calculate the lowest value
     *
     * @param numbers Zero or more numbers to compare
     * @return The lowest value
     */
    public static Number min(Number... numbers) {
        return Arrays.stream(numbers)
                .mapToDouble(Number::doubleValue)
                .min()
                .orElse(Double.MIN_VALUE);
    }

    public static class BoundingBox {
        private final double x;
        private final double y;

        public BoundingBox(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static class CoordinateOffsets {
        private final double topLeftX;
        private final double topLeftY;
        private final double topRightX;
        private final double topRightY;
        private final double bottomLeftX;
        private final double bottomLeftY;
        private final double bottomRightX;
        private final double bottomRightY;

        public CoordinateOffsets(double axisX, double axisY, double width, double height) {
            // Top left
            this.topLeftX = axisX;
            this.topLeftY = height - axisY;

            // Top right
            this.topRightX = width - axisX;
            this.topRightY = height - axisY;

            // Bottom right
            this.bottomLeftX = width - axisX;
            this.bottomLeftY = axisY;

            // Bottom left
            this.bottomRightX = axisX;
            this.bottomRightY = axisY;
        }

        public double getTopLeftX() {
            return topLeftX;
        }

        public double getTopLeftY() {
            return topLeftY;
        }

        public double getTopRightX() {
            return topRightX;
        }

        public double getTopRightY() {
            return topRightY;
        }

        public double getBottomLeftX() {
            return bottomLeftX;
        }

        public double getBottomLeftY() {
            return bottomLeftY;
        }

        public double getBottomRightX() {
            return bottomRightX;
        }

        public double getBottomRightY() {
            return bottomRightY;
        }
    }
}
