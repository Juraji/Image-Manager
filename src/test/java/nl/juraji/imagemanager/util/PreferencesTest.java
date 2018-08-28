package nl.juraji.imagemanager.util;

import nl.juraji.imagemanager.util.Preferences;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
class PreferencesTest {

    @Test
    void setPinterestLogin() {
        String username = "abc";
        String password = "def";

        Preferences.setPinterestLogin(username, password);
        final String[] pinterestLogin = Preferences.getPinterestLogin();

        assertEquals(2, pinterestLogin.length);
        assertEquals(username, pinterestLogin[0]);
        assertEquals(password, pinterestLogin[1]);
    }
}