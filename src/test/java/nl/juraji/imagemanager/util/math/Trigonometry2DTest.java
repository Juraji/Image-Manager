package nl.juraji.imagemanager.util.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
class Trigonometry2DTest {

    // Tolerate an error after 10 significant digits
    private final double toleranceDelta = 0.1E-10;

    @Test
    public void rotate() {
        final double deg0R90 = Trigonometry2D.rotate(0.0, 90);
        final double deg90R90 = Trigonometry2D.rotate(90.0, 90);
        final double deg180R90 = Trigonometry2D.rotate(180.0, 90);
        final double deg270R90 = Trigonometry2D.rotate(270.0, 90);
        final double deg0R90Neg = Trigonometry2D.rotate(0.0, -90);
        final double deg90R90Neg = Trigonometry2D.rotate(90.0, -90);
        final double deg180R90Neg = Trigonometry2D.rotate(180.0, -90);
        final double deg270R90Neg = Trigonometry2D.rotate(270.0, -90);
        final double deg285R70 = Trigonometry2D.rotate(345.0, 70);
        final double deg55R70Neg = Trigonometry2D.rotate(55.0, -70);

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
        final double[] degX5Y9R0 = Trigonometry2D.rotateCoordinates(0.0, 5, 9);
        final double[] degX5Y9R90 = Trigonometry2D.rotateCoordinates(90.0, 5, 9);
        final double[] degX5Y9R180 = Trigonometry2D.rotateCoordinates(180.0, 5, 9);
        final double[] degX5Y9R270 = Trigonometry2D.rotateCoordinates(270.0, 5, 9);
        final double[] degX5Y9R90Neg = Trigonometry2D.rotateCoordinates(-90.0, 5, 9);
        final double[] degX5Y9R180Neg = Trigonometry2D.rotateCoordinates(-180.0, 5, 9);
        final double[] degX5Y9R270Neg = Trigonometry2D.rotateCoordinates(-270.0, 5, 9);
        final double[] degX5Y9R345 = Trigonometry2D.rotateCoordinates(345.0, 5, 9);
        final double[] degX5Y9R55 = Trigonometry2D.rotateCoordinates(55.0, 5, 9);

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
    public void rotateCoordinates1() {
        final double[] degX5Y9R0 = Trigonometry2D.rotateCoordinates(0.0, 5, 9, 20, 20);
        final double[] degX5Y9R90 = Trigonometry2D.rotateCoordinates(90.0, 5, 9, 20, 20);
        final double[] degX5Y9R180 = Trigonometry2D.rotateCoordinates(180.0, 5, 9, 20, 20);
        final double[] degX5Y9R270 = Trigonometry2D.rotateCoordinates(270.0, 5, 9, 20, 20);
        final double[] degX5Y9R90Neg = Trigonometry2D.rotateCoordinates(-90.0, 5, 9, 20, 20);
        final double[] degX5Y9R180Neg = Trigonometry2D.rotateCoordinates(-180.0, 5, 9, 20, 20);
        final double[] degX5Y9R270Neg = Trigonometry2D.rotateCoordinates(-270.0, 5, 9, 20, 20);
        final double[] degX5Y9R345 = Trigonometry2D.rotateCoordinates(345.0, 5, 9, 20, 20);
        final double[] degX5Y9R55 = Trigonometry2D.rotateCoordinates(55.0, 5, 9, 20, 20);

        assertEquals(5.0, degX5Y9R0[0], toleranceDelta);
        assertEquals(9.0, degX5Y9R0[1], toleranceDelta);

        assertEquals(9.0, degX5Y9R90[0], toleranceDelta);
        assertEquals(35.0, degX5Y9R90[1], toleranceDelta);

        assertEquals(35.0, degX5Y9R180[0], toleranceDelta);
        assertEquals(31.0, degX5Y9R180[1], toleranceDelta);

        assertEquals(31.0, degX5Y9R270[0], toleranceDelta);
        assertEquals(5.0, degX5Y9R270[1], toleranceDelta);

        assertEquals(31.0, degX5Y9R90Neg[0], toleranceDelta);
        assertEquals(5.0, degX5Y9R90Neg[1], toleranceDelta);

        assertEquals(35.0, degX5Y9R180Neg[0], toleranceDelta);
        assertEquals(31.0, degX5Y9R180Neg[1], toleranceDelta);

        assertEquals(9.0, degX5Y9R270Neg[0], toleranceDelta);
        assertEquals(35.0, degX5Y9R270Neg[1], toleranceDelta);

        assertEquals(8.358122101791702, degX5Y9R345[0], toleranceDelta);
        assertEquals(5.492530234282439, degX5Y9R345[1], toleranceDelta);

        assertEquals(2.385680967555402, degX5Y9R55[0], toleranceDelta);
        assertEquals(25.97793986447337, degX5Y9R55[1], toleranceDelta);
    }

    @Test
    void coordinateOffsets() {
        final double[][] offsets = Trigonometry2D.coordinateOffsets(2, 2, 20, 10);

        assertEquals(2.0, offsets[0][0], toleranceDelta);
        assertEquals(8.0, offsets[0][1], toleranceDelta);
        assertEquals(18.0, offsets[1][0], toleranceDelta);
        assertEquals(8.0, offsets[1][1], toleranceDelta);
        assertEquals(18.0, offsets[2][0], toleranceDelta);
        assertEquals(2.0, offsets[2][1], toleranceDelta);
        assertEquals(2.0, offsets[3][0], toleranceDelta);
        assertEquals(2.0, offsets[3][1], toleranceDelta);
    }

    @Test
    void getBoundingBox() {
        double[] rotation30 = Trigonometry2D.getBoundingBox(30.0, 20,10);
        double[] rotation55 = Trigonometry2D.getBoundingBox(55.0, 20,10);
        double[] rotation90 = Trigonometry2D.getBoundingBox(90.0, 20,10);
        double[] rotation180 = Trigonometry2D.getBoundingBox(180.0, 20,10);
        double[] rotation260 = Trigonometry2D.getBoundingBox(260.0, 20,10);
        double[] rotationNeg30 = Trigonometry2D.getBoundingBox(-30.0, 20,10);
        double[] rotationNeg55 = Trigonometry2D.getBoundingBox(-55.0, 20,10);
        double[] rotationNeg90 = Trigonometry2D.getBoundingBox(-90.0, 20,10);
        double[] rotationNeg180 = Trigonometry2D.getBoundingBox(-180.0, 20,10);
        double[] rotationNeg260 = Trigonometry2D.getBoundingBox(-260.0, 20,10);

        // Tolerate an error after 10 significant digits
        final double toleranceDelta = 0.1E-10;

        assertEquals(22.320508075688778, rotation30[0], toleranceDelta);
        assertEquals(18.660254037844386, rotation30[1], toleranceDelta);

        assertEquals(19.663049169910835, rotation55[0], toleranceDelta);
        assertEquals(22.118805249290297, rotation55[1], toleranceDelta);

        assertEquals(10.0, rotation90[0], toleranceDelta);
        assertEquals(20.0, rotation90[1], toleranceDelta);

        assertEquals(20.0, rotation180[0], toleranceDelta);
        assertEquals(10.0, rotation180[1], toleranceDelta);

        assertEquals(13.321041083460685, rotation260[0], toleranceDelta);
        assertEquals(21.432636836913463, rotation260[1], toleranceDelta);

        assertEquals(22.32050807568877, rotationNeg30[0], toleranceDelta);
        assertEquals(18.660254037844393, rotationNeg30[1], toleranceDelta);

        assertEquals(19.663049169910835, rotationNeg55[0], toleranceDelta);
        assertEquals(22.118805249290297, rotationNeg55[1], toleranceDelta);

        assertEquals(10.0, rotationNeg90[0], toleranceDelta);
        assertEquals(20.0, rotationNeg90[1], toleranceDelta);

        assertEquals(20.0, rotationNeg180[0], toleranceDelta);
        assertEquals(10.0, rotationNeg180[1], toleranceDelta);

        assertEquals(13.321041083460685, rotationNeg260[0], toleranceDelta);
        assertEquals(21.432636836913463, rotationNeg260[1], toleranceDelta);
    }

    @Test
    void invertRotation() {
        final double Deg10 = Trigonometry2D.invertRotation(10.0);
        final double DegMin90 = Trigonometry2D.invertRotation(-90.0);
        final double Deg350 = Trigonometry2D.invertRotation(350.0);
        final double Deg25 = Trigonometry2D.invertRotation(25.0);

        assertEquals(190.0, Deg10);
        assertEquals(90.0, DegMin90);
        assertEquals(170.0, Deg350);
        assertEquals(205.0, Deg25);
    }

    @Test
    void absoluteRotation() {
        final double DegNeg5459 = Trigonometry2D.absoluteRotation(-5459.0);
        final double Deg5459 = Trigonometry2D.absoluteRotation(5459.0);
        final double DegNeg10 = Trigonometry2D.absoluteRotation(-10.0);
        final double Deg10 = Trigonometry2D.absoluteRotation(10.0);
        final double Deg360 = Trigonometry2D.absoluteRotation(360.0);

        assertEquals(301.0, DegNeg5459);
        assertEquals(59.0, Deg5459);
        assertEquals(350.0, DegNeg10);
        assertEquals(10.0, Deg10);
        assertEquals(0.0, Deg360);
    }

    @Test
    void max() {
    }

    @Test
    void min() {
    }
}