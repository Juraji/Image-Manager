package nl.juraji.imagemanager.model.pinterest;

import nl.juraji.imagemanager.model.ImageMetaData;
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
}
