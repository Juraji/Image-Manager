package nl.juraji.imagemanager.ui.dialogs;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.ImageViewer;
import nl.juraji.imagemanager.ui.components.SlideShowController;
import nl.juraji.imagemanager.ui.components.SlideShowController.SlideEvent;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.events.AcceleratorMap;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;
import nl.juraji.imagemanager.util.ui.traits.DialogStageConstructor;
import nl.juraji.imagemanager.util.ui.traits.FXMLConstructor;

import java.net.URL;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;

import static nl.juraji.imagemanager.ui.components.SlideShowController.SlideEvent.*;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class ViewImageDialog extends BorderPane implements FXMLConstructor, DialogStageConstructor, Initializable {

    private List<ImageMetaData> availableImageMetaData;
    private ImageMetaData imageMetaData;
    private EditableFieldContainer editableFieldContainer;
    private ResourceBundle resources;

    private final BooleanProperty otherMetaDataAvailable;

    @FXML
    private VBox editableFieldGroup;
    @FXML
    private ImageViewer imageViewer;
    @FXML
    private Label filePathTextField;
    @FXML
    private Label directoryNameTextField;
    @FXML
    private Label imageDimensionsTextField;
    @FXML
    private Label fileSizeTextField;
    @FXML
    private Label dateAddedTextField;
    @FXML
    private SlideShowController slideShowController;
    @FXML
    private Label acceleratorsLabel;

    public ViewImageDialog(ImageMetaData imageMetaData) {
        this.imageMetaData = imageMetaData;

        this.otherMetaDataAvailable = new SimpleBooleanProperty(false);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.reinitializeMetaData();

        final Tooltip acceleratorsTooltip = new Tooltip(getAccelerators().createTooltipText(resources));
        // Reset font to 12pt since the label is set to 27pt
        acceleratorsTooltip.setFont(Font.font(12));
        acceleratorsLabel.setTooltip(acceleratorsTooltip);
    }

    @Override
    public void preUnloadedFromView() {
        this.slideShowController.stop();
    }

    @Override
    public AcceleratorMap getAccelerators() {
        return new AcceleratorMap()
                .putKey(KeyCode.ESCAPE, this::close, "ViewImageDialog.accelerators.close.name")
                .putKey(KeyCode.LEFT, this::slideShowControllerOnSlidePrevious, "ViewImageDialog.accelerators.slideShowControllerOnSlidePrevious.name")
                .putKey(KeyCode.RIGHT, this::slideShowControllerOnSlideNext, "ViewImageDialog.accelerators.slideShowControllerOnSlideNext.name")
                .putKeyWithControl(KeyCode.RIGHT, this::slideShowControllerOnSlideNextRandom, "ViewImageDialog.accelerators.slideShowControllerOnSlideNextRandom.name")
                .putKeyWithControl(KeyCode.PERIOD, this.slideShowController::start, "ViewImageDialog.accelerators.start.name")
                .putKeyWithAlt(KeyCode.NUMPAD0, this.imageViewer::resetViewer, "ViewImageDialog.accelerators.resetViewer.name")
                .putKeyWithAlt(KeyCode.LEFT, this.imageViewer::rotateCounterclockwise90, "ViewImageDialog.accelerators.rotateCounterclockwise90.name")
                .putKeyWithAlt(KeyCode.RIGHT, this.imageViewer::rotateClockwise90, "ViewImageDialog.accelerators.rotateClockwise90.name")
                .putKeyWithAlt(KeyCode.DOWN, () -> {
                    this.imageViewer.setZoomStyle(ImageViewer.ZoomStyle.ZOOM_TO_FIT);
                    this.imageViewer.zoomToFit();
                }, "ViewImageDialog.accelerators.zoomToFit.name")
                .putKeyWithAlt(KeyCode.UP, () -> {
                    this.imageViewer.setZoomStyle(ImageViewer.ZoomStyle.ORIGINAL);
                    this.imageViewer.zoomToOriginalSize();
                }, "ViewImageDialog.accelerators.zoomToOriginal.name");
    }

    @Override
    public String getWindowTitle() {
        return resources.getString("ViewImageDialog.window.title");
    }

    public void setAvailableImageMetaData(List<ImageMetaData> availableImageMetaData) {
        this.availableImageMetaData = availableImageMetaData;
        this.otherMetaDataAvailable.setValue(availableImageMetaData != null);
    }

    public boolean isOtherMetaDataAvailable() {
        return otherMetaDataAvailable.get();
    }

    public ReadOnlyBooleanProperty otherMetaDataAvailableProperty() {
        return otherMetaDataAvailable;
    }

    private void reinitializeMetaData() {
        this.editableFieldContainer = EditableFieldContainer.create(imageMetaData);

        // Set information labels
        filePathTextField.setText(imageMetaData.getFile().getPath());
        directoryNameTextField.setText(imageMetaData.getDirectoryName());
        imageDimensionsTextField.setText(TextUtils.format(resources, "common.imageDimensions.label",
                imageMetaData.getImageWidth(), imageMetaData.getImageHeight()));
        fileSizeTextField.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));

        final String formattedDateAdded = UIUtils.formatDateTime(imageMetaData.getDateAdded(), FormatStyle.LONG);
        dateAddedTextField.setText(formattedDateAdded);

        // Remove existing editable fields
        editableFieldGroup.getChildren().clear();

        // Render editable fields
        editableFieldContainer.getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nLabelKey()));
            final Control control = fieldDefinition.getHandler().getControl();
            editableFieldGroup.getChildren().add(label);
            editableFieldGroup.getChildren().add(control);
        });

        // Load image into view
        Platform.runLater(() -> {
            final Image image = UIUtils.safeLoadImage(imageMetaData.getFile());
            imageViewer.setImage(image);
        });
    }

    @FXML
    private void toolbarSaveAction(ActionEvent actionEvent) {
        actionEvent.consume();
        final Stage toastStage = UIUtils.getStage(actionEvent);

        for (FieldDefinition field : this.editableFieldContainer.getFields()) {
            if (field.getHandler().isFieldInvalid()) {
                final String fieldName = resources.getString(field.getI18nLabelKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("ViewImageDialog.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("ViewImageDialog.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getTextValue(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        new Dao().save(imageMetaData);

        ToastBuilder.create(toastStage)
                .withMessage(resources.getString("ViewImageDialog.toolbarSaveAction.saved"), imageMetaData.getFile().getName())
                .show();
    }

    @FXML
    private void toolbarCloseAction() {
        this.close();
    }

    @FXML
    private void informationPaneFilePathLabelClicked(MouseEvent mouseEvent) {
        mouseEvent.consume();
        if (UIUtils.isDoublePrimaryClickEvent(mouseEvent)) {
            UIUtils.desktopOpen(imageMetaData.getFile().toURI());
        }
    }

    @FXML
    private void slideShowControllerOnSlideHandler(SlideEvent e) {
        if (NEXT_SLIDE_EVENT.equals(e.getEventType())) {
            this.slideShowControllerOnSlideNext();
        } else if (NEXT_RANDOM_SLIDE_EVENT.equals(e.getEventType())) {
            this.slideShowControllerOnSlideNextRandom();
        } else if (PREVIOUS_SLIDE_EVENT.equals(e.getEventType())) {
            this.slideShowControllerOnSlidePrevious();
        }
    }

    private void slideShowControllerOnSlideNext() {
        if (this.otherMetaDataAvailable.get()) {
            int index = availableImageMetaData.indexOf(imageMetaData) + 1;
            if (index == availableImageMetaData.size()) {
                index = 0;
            }

            imageMetaData = availableImageMetaData.get(index);
            this.reinitializeMetaData();
        }
    }

    private void slideShowControllerOnSlideNextRandom() {
        if (this.otherMetaDataAvailable.get()) {
            final int index = (int) (Math.random() * availableImageMetaData.size());
            imageMetaData = availableImageMetaData.get(index);
            this.reinitializeMetaData();
        }
    }

    private void slideShowControllerOnSlidePrevious() {
        if (this.otherMetaDataAvailable.get()) {
            int index = availableImageMetaData.indexOf(imageMetaData) - 1;
            if (index == -1) {
                index = availableImageMetaData.size() - 1;
            }

            imageMetaData = availableImageMetaData.get(index);
            this.reinitializeMetaData();
        }
    }
}
