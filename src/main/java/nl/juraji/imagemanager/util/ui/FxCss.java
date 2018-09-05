package nl.juraji.imagemanager.util.ui;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public class FxCss {

    public static String fontSize(int size) {
        return "-fx-font-size: " + size + ";";
    }

    public static String padding(int size) {
        return padding(size, size, size, size);
    }

    public static String padding(int top, int left, int bottom, int right) {
        return "-fx-padding: " + top + " " + left + " " + bottom + " " + right + ";";
    }

    public static String backgroundColor(int red, int green, int blue, double alpha) {
        return "-fx-background-color: rgba(" + red + "," + green + "," + blue + "," + alpha + ")";
    }
}
