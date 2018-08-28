package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.ui.modelfields.Editable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.net.URI;
import java.util.Map;

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
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = {@Parameter(name = "classname", value = "java.util.HashMap")})
    private Map<Integer, String> downloadUrls;

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

    public Map<Integer, String> getDownloadUrls() {
        return downloadUrls;
    }

    public void setDownloadUrls(Map<Integer, String> downloadUrls) {
        this.downloadUrls = downloadUrls;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
