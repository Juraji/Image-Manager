package nl.juraji.imagemanager.fxml.dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.components.ImageViewer;
import nl.juraji.imagemanager.components.builders.AlertBuilder;
import nl.juraji.imagemanager.components.builders.ToastBuilder;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.FXML;
import nl.juraji.imagemanager.util.ui.InitializableWithData;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;

import java.net.URL;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public class EditImageController implements InitializableWithData<ImageMetaData> {
    private ResourceBundle resources;
    private ImageMetaData imageMetaData;
    private EditableFieldContainer editableFieldContainer;

    public AnchorPane imageViewerContainer;
    public ImageViewer imageViewer;
    public VBox informationPaneVBox;
    public Label filePathTextField;
    public Label directoryNameTextField;
    public Label imageDimensionsTextField;
    public Label fileSizeTextField;
    public Label dateAddedTextField;
    public Label statusBarZoomLevelLabel;

    public static void showAsDialog(ImageMetaData imageMetaData) {
        final Parent imageView = FXML.createView(EditImageController.class, imageMetaData);

        final Stage stage = new Stage();
        stage.initOwner(Main.getPrimaryStage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(imageView));
        stage.setTitle(imageMetaData.getFile().getName());

        stage.show();
    }

    @Override
    public void initializeWithData(URL location, ResourceBundle resources, ImageMetaData data) {
        this.resources = resources;
        this.imageMetaData = data;
        this.editableFieldContainer = EditableFieldContainer.create(imageMetaData);

        statusBarZoomLevelLabel.textProperty().bind(imageViewer.zoomProperty()
                .multiply(100)
                .asString(resources.getString("editImageController.statusBarZoomLevel.label")));

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

    public void toolbarSaveAction(ActionEvent actionEvent) {
        final Stage toastStage = UIUtils.getStage(actionEvent);

        for (FieldDefinition field : this.editableFieldContainer.getFields()) {
            if (field.getHandler().isFieldInvalid()) {
                final String fieldName = resources.getString(field.getI18nLabelKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("editImageController.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("editImageController.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getTextValue(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        new Dao().save(imageMetaData);

        ToastBuilder.create(toastStage)
                .withMessage(resources.getString("editImageController.toolbarSaveAction.saved"), imageMetaData.getFile().getName())
                .show();
    }

    public void toolbarCloseAction(ActionEvent actionEvent) {
        UIUtils.getStage(actionEvent).close();
    }

    public void informationPaneFilePathLabelClicked(MouseEvent mouseEvent) {
        if (UIUtils.isDoublePrimaryClickEvent(mouseEvent)) {
            UIUtils.desktopOpen(imageMetaData.getFile().toURI());
        }
    }
}
