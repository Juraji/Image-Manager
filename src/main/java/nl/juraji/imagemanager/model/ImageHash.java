package nl.juraji.imagemanager.model;

import javax.persistence.*;
import java.util.BitSet;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
@Entity
public class ImageHash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false, length = 3000)
    private BitSet hash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Contrast contrast;

    @OneToOne(optional = false)
    private ImageMetaData imageMetaData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BitSet getHash() {
        return hash;
    }

    public Contrast getContrast() {
        return contrast;
    }

    public void setContrast(Contrast contrast) {
        this.contrast = contrast;
    }

    public void setHash(BitSet hash) {
        this.hash = hash;
    }

    public ImageMetaData getImageMetaData() {
        return imageMetaData;
    }

    public void setImageMetaData(ImageMetaData imageMetaData) {
        this.imageMetaData = imageMetaData;
    }
}
