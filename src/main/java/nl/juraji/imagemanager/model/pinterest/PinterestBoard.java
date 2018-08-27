package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.util.ui.modelfields.Editable;
import nl.juraji.imagemanager.util.ui.UIUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.net.URI;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
@Entity
public class PinterestBoard extends Directory {

    @Editable(labelResource = "model.fieldNames.pinterestBoard.boardUrl", order = 4)
    @Column(length = 1024, nullable = false)
    private URI boardUrl;

    public URI getBoardUrl() {
        return boardUrl;
    }

    public void setBoardUrl(URI boardUrl) {
        this.boardUrl = boardUrl;
    }

    // UI properties
    @Override
    public void desktopOpenSource() {
        UIUtils.desktopOpen(getBoardUrl());
    }
}
