package nl.juraji.imagemanager.ui.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.ui.builders.ChoiceProperty;
import nl.juraji.imagemanager.ui.builders.DirectoryChooserBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.util.BorderPaneScene;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.ResourceUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.io.pinterest.PinterestWebSession;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class SettingsScene extends BorderPaneScene {

    private Locale currentLocale;

    @FXML
    private ChoiceBox<ChoiceProperty<Locale>> applicationLocaleChoiceBox;
    @FXML
    private TextField pinterestTargetLocationTextField;
    @FXML
    private TextField pinterestUsernameTextField;
    @FXML
    private PasswordField pinterestPasswordField;
    @FXML
    private CheckBox applicationDebugMode;

    public SettingsScene() {
        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        // Setup language field
        final List<ChoiceProperty<Locale>> availableLocales = ResourceUtils.getAvailableLocales().stream()
                .map(l -> new ChoiceProperty<>(l.getDisplayLanguage(), l))
                .collect(Collectors.toList());

        this.currentLocale = Preferences.getLocale();
        final ChoiceProperty<Locale> current = availableLocales.stream()
                .filter(l -> l.getValue().equals(currentLocale))
                .findFirst()
                .orElse(null);

        applicationLocaleChoiceBox.getItems().addAll(availableLocales);
        applicationLocaleChoiceBox.setValue(current);

        // Setup debug mode check box
        applicationDebugMode.setSelected(Preferences.isDebugMode());

        // Setup Pinterest fields
        pinterestTargetLocationTextField.setText(Preferences.getPinterestTargetDirectory().getAbsolutePath());

        final String[] pinterestLogin = Preferences.getPinterestLogin();
        if (pinterestLogin != null) {
            pinterestUsernameTextField.setText(pinterestLogin[0]);
        }
    }

    @FXML
    public void toolbarBackAction(MouseEvent mouseEvent) {
        Main.getPrimaryScene().previousContent();
    }

    @FXML
    public void toolbarSaveAction(MouseEvent mouseEvent) {
        boolean languageChanged = false;

        // Save language
        final Locale choiceLocale = applicationLocaleChoiceBox.getValue().getValue();
        if (!currentLocale.equals(choiceLocale)) {
            Preferences.setLocale(choiceLocale);
            languageChanged = true;
        }

        // Save debug mode
        Preferences.setDebugMode(applicationDebugMode.isSelected());

        // Save Pinterest settings
        final String newTargetLocation = pinterestTargetLocationTextField.getText();
        if (!TextUtils.isEmpty(newTargetLocation)) {
            final File file = new File(newTargetLocation);
            Preferences.setPinterestTargetDirectory(file);
        }

        final String pinterestUsername = pinterestUsernameTextField.getText();
        final String pinterestPassword = pinterestPasswordField.getText();
        if (!TextUtils.isEmpty(pinterestUsername, pinterestPassword)) {
            Preferences.setPinterestLogin(pinterestUsername, pinterestPassword);

            try {
                PinterestWebSession.getCookieJar().deleteCookies();
            } catch (IOException ignored) {
            }
        }

        // Reload scene if language changed
        if (languageChanged) {
            Main.getPrimaryScene().pushContent(new SettingsScene(), true);
        }

        ToastBuilder.create()
                .withMessage(resources.getString("SettingsScene.toolbar.saveAction.toast"))
                .show();
    }

    @FXML
    public void pinterestChooseTargetDirectoryAction(MouseEvent mouseEvent) {
        DirectoryChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("SettingsScene.panes.pinterest.targetLocation.browse.title"))
                .show(f -> pinterestTargetLocationTextField.setText(f.getAbsolutePath()));
    }
}
