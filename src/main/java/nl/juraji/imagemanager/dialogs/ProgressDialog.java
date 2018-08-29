package nl.juraji.imagemanager.dialogs;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public final class ProgressDialog {
    private static final double DIALOG_WIDTH = 300.0;
    private static final double DIALOG_PADDING = 5.0;

    private final Stage dialogStage;
    private final ProgressBar progressBar;
    private final Text descriptionLabel;
    private final ETCText etaLabel;

    public ProgressDialog(Window owner, String title) {
        dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setWidth(DIALOG_WIDTH);
        dialogStage.setTitle(title);

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(DIALOG_WIDTH);

        etaLabel = new ETCText("ETC: ");

        descriptionLabel = new Text();
        descriptionLabel.setWrappingWidth(DIALOG_WIDTH);

        final VBox box = new VBox();
        box.setSpacing(DIALOG_PADDING);
        box.setPadding(new Insets(DIALOG_PADDING));
        box.setSpacing(DIALOG_PADDING * 2);
        box.setAlignment(Pos.BASELINE_LEFT);
        box.getChildren().addAll(descriptionLabel, etaLabel, progressBar);

        Scene scene = new Scene(box);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task, String description) {
        if (task == null || task.isDone()) {
            // Do not show when task is already done
            return;
        }

        // Set description
        descriptionLabel.setText(description);

        // Unbind any current bindings and bind new task
        progressBar.progressProperty().unbind();
        progressBar.setProgress(-1);
        progressBar.progressProperty().bind(task.progressProperty());

        etaLabel.progressProperty().unbind();
        etaLabel.setProgress(-1);
        etaLabel.progressProperty().bind(task.progressProperty());

        dialogStage.show();
    }

    public void close() {
        this.dialogStage.close();
    }
}
