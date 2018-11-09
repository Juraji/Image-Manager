package nl.juraji.imagemanager.util.math;

import nl.juraji.imagemanager.util.math.Trigonometry2D.BoundingBox;
import nl.juraji.imagemanager.util.math.Trigonometry2D.CoordinateOffsets;
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
        // Clockwise
        final double deg0R90 = Trigonometry2D.rotate(0.0, 90);
        assertEquals(90.0, deg0R90);
        final double deg90R90 = Trigonometry2D.rotate(90.0, 90);
        assertEquals(180.0, deg90R90);
        final double deg180R90 = Trigonometry2D.rotate(180.0, 90);
        assertEquals(270.0, deg180R90);
        final double deg270R90 = Trigonometry2D.rotate(270.0, 90);
        assertEquals(0.0, deg270R90);

        // Counterclockwise
        final double deg0R90Neg = Trigonometry2D.rotate(0.0, -90);
        assertEquals(270.0, deg0R90Neg);
        final double deg90R90Neg = Trigonometry2D.rotate(90.0, -90);
        assertEquals(0.0, deg90R90Neg);
        final double deg180R90Neg = Trigonometry2D.rotate(180.0, -90);
        assertEquals(90.0, deg180R90Neg);
        final double deg270R90Neg = Trigonometry2D.rotate(270.0, -90);
        assertEquals(180.0, deg270R90Neg);

        // Zero crossing
        final double deg285R70 = Trigonometry2D.rotate(345.0, 70);
        assertEquals(55.0, deg285R70);
        final double deg55R70Neg = Trigonometry2D.rotate(55.0, -70);
        assertEquals(345.0, deg55R70Neg);
    }

    @Test
    public void rotateCoordinates() {
        final BoundingBox degX5Y9R0 = Trigonometry2D.rotateCoordinates(0.0, 5, 9);
        assertEquals(5.0, degX5Y9R0.getX(), toleranceDelta);
        assertEquals(9.0, degX5Y9R0.getY(), toleranceDelta);

        final BoundingBox degX5Y9R90 = Trigonometry2D.rotateCoordinates(90.0, 5, 9);
        assertEquals(9.0, degX5Y9R90.getX(), toleranceDelta);
        assertEquals(-5.0, degX5Y9R90.getY(), toleranceDelta);

        final BoundingBox degX5Y9R180 = Trigonometry2D.rotateCoordinates(180.0, 5, 9);
        assertEquals(-5.0, degX5Y9R180.getX(), toleranceDelta);
        assertEquals(-9.0, degX5Y9R180.getY(), toleranceDelta);

        final BoundingBox degX5Y9R270 = Trigonometry2D.rotateCoordinates(270.0, 5, 9);
        assertEquals(-9.0, degX5Y9R270.getX(), toleranceDelta);
        assertEquals(5.0, degX5Y9R270.getY(), toleranceDelta);

        final BoundingBox degX5Y9R90Neg = Trigonometry2D.rotateCoordinates(-90.0, 5, 9);
        assertEquals(-9.0, degX5Y9R90Neg.getX(), toleranceDelta);
        assertEquals(5.0, degX5Y9R90Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R180Neg = Trigonometry2D.rotateCoordinates(-180.0, 5, 9);
        assertEquals(-5.0, degX5Y9R180Neg.getX(), toleranceDelta);
        assertEquals(-9.0, degX5Y9R180Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R270Neg = Trigonometry2D.rotateCoordinates(-270.0, 5, 9);
        assertEquals(9.0, degX5Y9R270Neg.getX(), toleranceDelta);
        assertEquals(-5.0, degX5Y9R270Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R345 = Trigonometry2D.rotateCoordinates(345.0, 5, 9);
        assertEquals(2.5002577255226552, degX5Y9R345.getX(), toleranceDelta);
        assertEquals(9.987427662114218, degX5Y9R345.getY(), toleranceDelta);

        final BoundingBox degX5Y9R55 = Trigonometry2D.rotateCoordinates(55.0, 5, 9);
        assertEquals(10.240250580356157, degX5Y9R55.getX(), toleranceDelta);
        assertEquals(1.0664277057144558, degX5Y9R55.getY(), toleranceDelta);
    }

    @Test
    public void rotateCoordinates1() {
        final BoundingBox degX5Y9R0 = Trigonometry2D.rotateCoordinates(0.0, 5, 9, 20, 20);
        assertEquals(5.0, degX5Y9R0.getX(), toleranceDelta);
        assertEquals(9.0, degX5Y9R0.getY(), toleranceDelta);

        final BoundingBox degX5Y9R90 = Trigonometry2D.rotateCoordinates(90.0, 5, 9, 20, 20);
        assertEquals(9.0, degX5Y9R90.getX(), toleranceDelta);
        assertEquals(35.0, degX5Y9R90.getY(), toleranceDelta);

        final BoundingBox degX5Y9R180 = Trigonometry2D.rotateCoordinates(180.0, 5, 9, 20, 20);
        assertEquals(35.0, degX5Y9R180.getX(), toleranceDelta);
        assertEquals(31.0, degX5Y9R180.getY(), toleranceDelta);

        final BoundingBox degX5Y9R270 = Trigonometry2D.rotateCoordinates(270.0, 5, 9, 20, 20);
        assertEquals(31.0, degX5Y9R270.getX(), toleranceDelta);
        assertEquals(5.0, degX5Y9R270.getY(), toleranceDelta);

        final BoundingBox degX5Y9R90Neg = Trigonometry2D.rotateCoordinates(-90.0, 5, 9, 20, 20);
        assertEquals(31.0, degX5Y9R90Neg.getX(), toleranceDelta);
        assertEquals(5.0, degX5Y9R90Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R180Neg = Trigonometry2D.rotateCoordinates(-180.0, 5, 9, 20, 20);
        assertEquals(35.0, degX5Y9R180Neg.getX(), toleranceDelta);
        assertEquals(31.0, degX5Y9R180Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R270Neg = Trigonometry2D.rotateCoordinates(-270.0, 5, 9, 20, 20);
        assertEquals(9.0, degX5Y9R270Neg.getX(), toleranceDelta);
        assertEquals(35.0, degX5Y9R270Neg.getY(), toleranceDelta);

        final BoundingBox degX5Y9R345 = Trigonometry2D.rotateCoordinates(345.0, 5, 9, 20, 20);
        assertEquals(8.358122101791702, degX5Y9R345.getX(), toleranceDelta);
        assertEquals(5.492530234282439, degX5Y9R345.getY(), toleranceDelta);

        final BoundingBox degX5Y9R55 = Trigonometry2D.rotateCoordinates(55.0, 5, 9, 20, 20);
        assertEquals(2.385680967555402, degX5Y9R55.getX(), toleranceDelta);
        assertEquals(25.97793986447337, degX5Y9R55.getY(), toleranceDelta);
    }

    @Test
    void coordinateOffsets() {
        final CoordinateOffsets offsets = Trigonometry2D.coordinateOffsets(2, 2, 20, 10);

        assertEquals(2.0, offsets.getTopLeftX(), toleranceDelta);
        assertEquals(8.0, offsets.getTopLeftY(), toleranceDelta);
        assertEquals(18.0, offsets.getTopRightX(), toleranceDelta);
        assertEquals(8.0, offsets.getTopRightY(), toleranceDelta);
        assertEquals(18.0, offsets.getBottomLeftX(), toleranceDelta);
        assertEquals(2.0, offsets.getBottomLeftY(), toleranceDelta);
        assertEquals(2.0, offsets.getBottomRightY(), toleranceDelta);
        assertEquals(2.0, offsets.getBottomRightX(), toleranceDelta);
    }

    @Test
    void getBoundingBox() {
        // Tolerate an error after 10 significant digits
        final double toleranceDelta = 0.1E-10;

        BoundingBox rotation30 = Trigonometry2D.getBoundingBox(30.0, 20, 10);
        assertEquals(22.320508075688778, rotation30.getX(), toleranceDelta);
        assertEquals(18.660254037844386, rotation30.getY(), toleranceDelta);

        BoundingBox rotation55 = Trigonometry2D.getBoundingBox(55.0, 20, 10);
        assertEquals(19.663049169910835, rotation55.getX(), toleranceDelta);
        assertEquals(22.118805249290297, rotation55.getY(), toleranceDelta);

        BoundingBox rotation90 = Trigonometry2D.getBoundingBox(90.0, 20, 10);
        assertEquals(10.0, rotation90.getX(), toleranceDelta);
        assertEquals(20.0, rotation90.getY(), toleranceDelta);

        BoundingBox rotation180 = Trigonometry2D.getBoundingBox(180.0, 20, 10);
        assertEquals(20.0, rotation180.getX(), toleranceDelta);
        assertEquals(10.0, rotation180.getY(), toleranceDelta);

        BoundingBox rotation260 = Trigonometry2D.getBoundingBox(260.0, 20, 10);
        assertEquals(13.321041083460685, rotation260.getX(), toleranceDelta);
        assertEquals(21.432636836913463, rotation260.getY(), toleranceDelta);

        BoundingBox rotationNeg30 = Trigonometry2D.getBoundingBox(-30.0, 20, 10);
        assertEquals(22.32050807568877, rotationNeg30.getX(), toleranceDelta);
        assertEquals(18.660254037844393, rotationNeg30.getY(), toleranceDelta);

        BoundingBox rotationNeg55 = Trigonometry2D.getBoundingBox(-55.0, 20, 10);
        assertEquals(19.663049169910835, rotationNeg55.getX(), toleranceDelta);
        assertEquals(22.118805249290297, rotationNeg55.getY(), toleranceDelta);

        BoundingBox rotationNeg90 = Trigonometry2D.getBoundingBox(-90.0, 20, 10);
        assertEquals(10.0, rotationNeg90.getX(), toleranceDelta);
        assertEquals(20.0, rotationNeg90.getY(), toleranceDelta);

        BoundingBox rotationNeg180 = Trigonometry2D.getBoundingBox(-180.0, 20, 10);
        assertEquals(20.0, rotationNeg180.getX(), toleranceDelta);
        assertEquals(10.0, rotationNeg180.getY(), toleranceDelta);

        BoundingBox rotationNeg260 = Trigonometry2D.getBoundingBox(-260.0, 20, 10);
        assertEquals(13.321041083460685, rotationNeg260.getX(), toleranceDelta);
        assertEquals(21.432636836913463, rotationNeg260.getY(), toleranceDelta);
    }

    @Test
    void invertRotation() {
        final double Deg10 = Trigonometry2D.invertRotation(10.0);
        assertEquals(190.0, Deg10);
        final double DegMin90 = Trigonometry2D.invertRotation(-90.0);
        assertEquals(90.0, DegMin90);
        final double Deg350 = Trigonometry2D.invertRotation(350.0);
        assertEquals(170.0, Deg350);
        final double Deg25 = Trigonometry2D.invertRotation(25.0);
        assertEquals(205.0, Deg25);
    }

    @Test
    void absoluteRotation() {
        final double DegNeg5459 = Trigonometry2D.absoluteRotation(-5459.0);
        assertEquals(301.0, DegNeg5459);
        final double Deg5459 = Trigonometry2D.absoluteRotation(5459.0);
        assertEquals(59.0, Deg5459);
        final double DegNeg10 = Trigonometry2D.absoluteRotation(-10.0);
        assertEquals(350.0, DegNeg10);
        final double Deg10 = Trigonometry2D.absoluteRotation(10.0);
        assertEquals(10.0, Deg10);
        final double Deg360 = Trigonometry2D.absoluteRotation(360.0);
        assertEquals(0.0, Deg360);
    }

    @Test
    void max() {
        final Number max = Trigonometry2D.max(1, 2, 3, 6, 5, 4);
        assertEquals(6.0, max);

        final Number maxNeg = Trigonometry2D.max(-1, -2, -3, -6, -5, -4);
        assertEquals(-1.0, maxNeg);
    }

    @Test
    void min() {
        final Number min = Trigonometry2D.min(1, 2, 3, 6, 5, 4);
        assertEquals(1.0, min);

        final Number minNeg = Trigonometry2D.min(-1, -2, -3, -6, -5, -4);
        assertEquals(-6.0, minNeg);
    }
}