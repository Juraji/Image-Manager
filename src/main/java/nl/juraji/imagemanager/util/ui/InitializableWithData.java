package nl.juraji.imagemanager.util.ui;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public interface InitializableWithData<T> {
    void initializeWithData(URL location, ResourceBundle resources, T data);
}
