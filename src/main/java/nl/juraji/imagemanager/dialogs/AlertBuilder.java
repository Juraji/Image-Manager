package nl.juraji.imagemanager.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.juraji.imagemanager.util.TextUtils;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class AlertBuilder {
    private final Alert alert;

    private AlertBuilder(Alert.AlertType alertType) {
        alert = new Alert(alertType);
        alert.setHeaderText(null);
    }

    public static AlertBuilder createConfirm() {
        return new AlertBuilder(Alert.AlertType.CONFIRMATION);
    }

    public static AlertBuilder createInfo() {
        return new AlertBuilder(Alert.AlertType.INFORMATION);
    }

    public static AlertBuilder createWarning() {
        return new AlertBuilder(Alert.AlertType.WARNING);
    }

    public AlertBuilder withTitle(String title, Object... params) {
        alert.setTitle(TextUtils.format(title, params));
        return this;
    }

    public AlertBuilder withContext(String context, Object... params) {
        alert.setContentText(TextUtils.format(context, params));
        return this;
    }

    public void show() {
        alert.show();
    }

    public void show(Runnable work) {
        alert.showAndWait().ifPresent(t -> {
            if (t == ButtonType.OK) work.run();
        });
    }
}
