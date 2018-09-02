package nl.juraji.imagemanager.fxml.dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.juraji.imagemanager.Main;
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
import java.util.ResourceBundle;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public class EditImageController implements InitializableWithData<ImageMetaData> {
    private ResourceBundle resources;
    private ImageMetaData imageMetaData;
    private EditableFieldContainer editableFieldContainer;

    public GridPane informationGrid;
    public AnchorPane imageViewContainer;
    public StackPane imageViewWrapper;
    public ImageView imageView;

    public Label filePathTextField;
    public Label directoryNameTextField;
    public Label imageDimensionsTextField;
    public Label fileSizeTextField;
    public Label dateAddedTextField;

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

        // Bind image view fit dimensions to container dimensions
        final double padding = imageViewContainer.getPadding().getTop() * 2;
        imageView.fitWidthProperty().bind(imageViewContainer.widthProperty().subtract(padding));
        imageView.fitHeightProperty().bind(imageViewContainer.heightProperty().subtract(padding));

        // translate the image view wrapper to center on the container
        imageViewWrapper.translateXProperty().bind(imageViewContainer.widthProperty()
                .subtract(imageViewWrapper.widthProperty().divide(2)
                        .add(imageView.fitWidthProperty().divide(2)))
                .subtract(padding));
        imageViewWrapper.translateYProperty().bind(imageViewContainer.heightProperty()
                .subtract(imageViewWrapper.heightProperty().divide(2)
                        .add(imageView.fitHeightProperty().divide(2)))
                .subtract(padding));

        // Load image into view
        Platform.runLater(() -> {
            final Image image = UIUtils.safeLoadImage(imageMetaData.getFile());
            imageView.setImage(image);
        });

        // Set information labels
        filePathTextField.setText(imageMetaData.getFile().getPath());
        directoryNameTextField.setText(imageMetaData.getDirectoryName());
        imageDimensionsTextField.setText(TextUtils.format(resources, "common.imageDimensions.label",
                imageMetaData.getImageWidth(), imageMetaData.getImageHeight()));
        fileSizeTextField.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));
        dateAddedTextField.setText(imageMetaData.getDateAdded().toString());

        // Render editable fields
        editableFieldContainer.renderFieldsToGrid(informationGrid, resources, 5);
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
