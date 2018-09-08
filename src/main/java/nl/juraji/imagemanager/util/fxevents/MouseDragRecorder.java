package nl.juraji.imagemanager.util.fxevents;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public class MouseDragRecorder {
    private final SimpleObjectProperty<DragRecord> dragRecord;

    private final AtomicDouble currentDragStartY = new AtomicDouble();
    private final AtomicDouble currentDragStartX = new AtomicDouble();
    private Node deadZoneNode;

    public MouseDragRecorder(Node target) {
        this.dragRecord = new SimpleObjectProperty<>(new DragRecord(0, 0, 0, 0));
        target.setOnMousePressed(this::handleMousePressed);
        target.setOnMouseDragged(this::handleDrag);
    }

    public SimpleObjectProperty<DragRecord> dragRecordProperty() {
        return dragRecord;
    }

    public void setDeadZoneNode(Node deadZoneNode) {
        this.deadZoneNode = deadZoneNode;
    }

    private void handleMousePressed(MouseEvent e) {
        this.currentDragStartX.set(e.getSceneX());
        this.currentDragStartY.set(e.getSceneY());
    }

    private void handleDrag(MouseEvent e) {
        final boolean isInDeadZone = this.isInDeadZone(e.getSceneX(), e.getSceneY());

        if (!isInDeadZone) {
            final DragRecord dragRecord = new DragRecord(
                    currentDragStartX.get(),
                    currentDragStartY.get(),
                    e.getSceneX(),
                    e.getSceneY()
            );

            this.dragRecord.setValue(dragRecord);
        }

        currentDragStartX.set(e.getSceneX());
        currentDragStartY.set(e.getSceneY());
    }

    private boolean isInDeadZone(double x, double y) {
        if (this.deadZoneNode != null) {
            final Bounds bounds = deadZoneNode.localToScene(deadZoneNode.getBoundsInLocal());
            return bounds.contains(x, y);
        } else {
            return false;
        }
    }

    public final class DragRecord {

        private final double startX;
        private final double startY;
        private final double endX;
        private final double endY;

        public DragRecord(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public double getStartX() {
            return startX;
        }

        public double getStartY() {
            return startY;
        }

        public double getEndX() {
            return endX;
        }

        public double getEndY() {
            return endY;
        }

        public double getDeltaX() {
            return -(startX - endX);
        }

        public double getDeltaY() {
            return -(startY - endY);
        }

        @Override
        public String toString() {
            return "DragRecord{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", endX=" + endX +
                    ", endY=" + endY +
                    ", deltaX=" + getDeltaX() +
                    ", deltaY=" + getDeltaY() +
                    '}';
        }
    }
}
