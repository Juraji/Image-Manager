package nl.juraji.imagemanager.util.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
class RotationTest {

    @Test
    public void rotate() {
        final double deg0R90 = Rotation.rotate(0.0, 90);
        final double deg90R90 = Rotation.rotate(90.0, 90);
        final double deg180R90 = Rotation.rotate(180.0, 90);
        final double deg270R90 = Rotation.rotate(270.0, 90);
        final double deg0R90Neg = Rotation.rotate(0.0, -90);
        final double deg90R90Neg = Rotation.rotate(90.0, -90);
        final double deg180R90Neg = Rotation.rotate(180.0, -90);
        final double deg270R90Neg = Rotation.rotate(270.0, -90);
        final double deg285R70 = Rotation.rotate(345.0, 70);
        final double deg55R70Neg = Rotation.rotate(55.0, -70);

        // Clockwise
        assertEquals(90.0, deg0R90);
        assertEquals(180.0, deg90R90);
        assertEquals(270.0, deg180R90);
        assertEquals(0.0, deg270R90);

        // Counterclockwise
        assertEquals(270.0, deg0R90Neg);
        assertEquals(0.0, deg90R90Neg);
        assertEquals(90.0, deg180R90Neg);
        assertEquals(180.0, deg270R90Neg);

        // Zero crossing
        assertEquals(55.0, deg285R70);
        assertEquals(345.0, deg55R70Neg);
    }

    @Test
    public void rotateCoordinates() {
        final double[] degX5Y9R0 = Rotation.rotateCoordinates(0.0, 5, 9);
        final double[] degX5Y9R90 = Rotation.rotateCoordinates(90.0, 5, 9);
        final double[] degX5Y9R180 = Rotation.rotateCoordinates(180.0, 5, 9);
        final double[] degX5Y9R270 = Rotation.rotateCoordinates(270.0, 5, 9);
        final double[] degX5Y9R90Neg = Rotation.rotateCoordinates(-90.0, 5, 9);
        final double[] degX5Y9R180Neg = Rotation.rotateCoordinates(-180.0, 5, 9);
        final double[] degX5Y9R270Neg = Rotation.rotateCoordinates(-270.0, 5, 9);
        final double[] degX5Y9R345 = Rotation.rotateCoordinates(345.0, 5, 9);
        final double[] degX5Y9R55 = Rotation.rotateCoordinates(55.0, 5, 9);

        // Tolerate an error after 10 significant digits
        final double toleranceDelta = 0.1E-10;

        assertEquals(5.0, degX5Y9R0[0], toleranceDelta);
        assertEquals(9.0, degX5Y9R0[1], toleranceDelta);

        assertEquals(9.0, degX5Y9R90[0], toleranceDelta);
        assertEquals(-5.0, degX5Y9R90[1], toleranceDelta);

        assertEquals(-5.0, degX5Y9R180[0], toleranceDelta);
        assertEquals(-9.0, degX5Y9R180[1], toleranceDelta);

        assertEquals(-9.0, degX5Y9R270[0], toleranceDelta);
        assertEquals(5.0, degX5Y9R270[1], toleranceDelta);

        assertEquals(-9.0, degX5Y9R90Neg[0], toleranceDelta);
        assertEquals(5.0, degX5Y9R90Neg[1], toleranceDelta);

        assertEquals(-5.0, degX5Y9R180Neg[0], toleranceDelta);
        assertEquals(-9.0, degX5Y9R180Neg[1], toleranceDelta);

        assertEquals(9.0, degX5Y9R270Neg[0], toleranceDelta);
        assertEquals(-5.0, degX5Y9R270Neg[1], toleranceDelta);

        assertEquals(2.5002577255226552, degX5Y9R345[0], toleranceDelta);
        assertEquals(9.987427662114218, degX5Y9R345[1], toleranceDelta);

        assertEquals(10.240250580356157, degX5Y9R55[0], toleranceDelta);
        assertEquals(1.0664277057144558, degX5Y9R55[1], toleranceDelta);
    }

    @Test
    public void invert() {
        final double Deg10 = Rotation.invert(10.0);
        final double DegMin90 = Rotation.invert(-90.0);
        final double Deg350 = Rotation.invert(350.0);
        final double Deg25 = Rotation.invert(25.0);

        assertEquals(190.0, Deg10);
        assertEquals(90.0, DegMin90);
        assertEquals(170.0, Deg350);
        assertEquals(205.0, Deg25);
    }

    @Test
    public void absoluteDeg() {
        final double DegNeg5459 = Rotation.absoluteDeg(-5459.0);
        final double Deg5459 = Rotation.absoluteDeg(5459.0);
        final double DegNeg10 = Rotation.absoluteDeg(-10.0);
        final double Deg10 = Rotation.absoluteDeg(10.0);
        final double Deg360 = Rotation.absoluteDeg(360.0);

        assertEquals(301.0, DegNeg5459);
        assertEquals(59.0, Deg5459);
        assertEquals(350.0, DegNeg10);
        assertEquals(10.0, Deg10);
        assertEquals(0.0, Deg360);
    }
}