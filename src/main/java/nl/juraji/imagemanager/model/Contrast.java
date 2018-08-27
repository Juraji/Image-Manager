package nl.juraji.imagemanager.model;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
public enum Contrast {
    LIGHT, DARK;

    public static final int RGB_THRESHOLD = 152;

    public static Contrast forRGB(int RGB) {
        return RGB < RGB_THRESHOLD ? DARK : LIGHT;
    }
}
