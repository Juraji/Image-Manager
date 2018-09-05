package nl.juraji.imagemanager.ui.dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.ImageViewer;
import nl.juraji.imagemanager.ui.util.DialogStageConstructor;
import nl.juraji.imagemanager.ui.util.FXMLConstructor;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;

import java.net.URL;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class EditImageDialog extends BorderPane implements FXMLConstructor, DialogStageConstructor, Initializable {

    private final ImageMetaData imageMetaData;
    private final EditableFieldContainer editableFieldContainer;
    private ResourceBundle resources;

    @FXML
    private ImageViewer imageViewer;
    @FXML
    private VBox informationPaneVBox;
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

    public EditImageDialog(ImageMetaData imageMetaData) {
        this.imageMetaData = imageMetaData;
        this.editableFieldContainer = EditableFieldContainer.create(imageMetaData);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        // Set information labels
        filePathTextField.setText(imageMetaData.getFile().getPath());
        directoryNameTextField.setText(imageMetaData.getDirectoryName());
        imageDimensionsTextField.setText(TextUtils.format(resources, "common.imageDimensions.label",
                imageMetaData.getImageWidth(), imageMetaData.getImageHeight()));
        fileSizeTextField.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));

        final String formattedDateAdded = UIUtils.formatDateTime(imageMetaData.getDateAdded(), FormatStyle.LONG);
        dateAddedTextField.setText(formattedDateAdded);

        // Render editable fields
        editableFieldContainer.getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nLabelKey()));
            final Control control = fieldDefinition.getHandler().getControl();
            informationPaneVBox.getChildren().add(label);
            informationPaneVBox.getChildren().add(control);
        });

        // Load image into view
        Platform.runLater(() -> {
            final Image image = UIUtils.safeLoadImage(imageMetaData.getFile());
            imageViewer.setImage(image);
        });
    }

    @FXML
    private void toolbarSaveAction(ActionEvent actionEvent) {
        final Stage toastStage = UIUtils.getStage(actionEvent);

        for (FieldDefinition field : this.editableFieldContainer.getFields()) {
            if (field.getHandler().isFieldInvalid()) {
                final String fieldName = resources.getString(field.getI18nLabelKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("EditImageDialog.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("EditImageDialog.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getTextValue(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        new Dao().save(imageMetaData);

        ToastBuilder.create(toastStage)
                .withMessage(resources.getString("EditImageDialog.toolbarSaveAction.saved"), imageMetaData.getFile().getName())
                .show();
    }

    @FXML
    private void toolbarCloseAction(ActionEvent actionEvent) {
        UIUtils.getStage(actionEvent).close();
    }

    @FXML
    private void informationPaneFilePathLabelClicked(MouseEvent mouseEvent) {
        if (UIUtils.isDoublePrimaryClickEvent(mouseEvent)) {
            UIUtils.desktopOpen(imageMetaData.getFile().toURI());
        }
    }
}
