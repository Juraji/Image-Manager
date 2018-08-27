package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.ui.modelfields.Editable;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.net.URI;
import java.util.Set;

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
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private Set<String> downloadUrls;

    @Editable(labelResource = "model.fieldNames.pinMetaData.description", order = 0)
    @Column(length = 2048)
    private String description;

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

    public Set<String> getDownloadUrls() {
        return downloadUrls;
    }

    public void setDownloadUrls(Set<String> downloadUrls) {
        this.downloadUrls = downloadUrls;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
