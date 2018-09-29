package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.ImageMetaData;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.net.URI;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
@Entity
public class PinMetaData extends ImageMetaData {

    @Column
    private String pinId;

    @Column
    private URI pinterestUri;

    @Column
    private URI downloadUrl;

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }

    public URI getPinterestUri() {
        return pinterestUri;
    }

    public void setPinterestUri(URI pinterestUri) {
        this.pinterestUri = pinterestUri;
    }

    public URI getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
