package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.Directory;
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

    @Column(length = 1024, nullable = false)
    private URI boardUrl;

    @Column(nullable = false)
    private String boardId;

    public URI getBoardUrl() {
        return boardUrl;
    }

    public void setBoardUrl(URI boardUrl) {
        this.boardUrl = boardUrl;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    @Override
    public String getSourceType() {
        return "Pinterest";
    }

    // UI properties
    @Override
    public void desktopOpenSource() {
        UIUtils.desktopOpen(getBoardUrl());
    }
}
