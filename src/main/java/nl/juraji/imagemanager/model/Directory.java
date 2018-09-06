package nl.juraji.imagemanager.model;

import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.modelfields.Editable;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Directory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Editable(labelResource = "model.fieldNames.directory.name", order = 0)
    @Column(nullable = false)
    private String name;

    @Editable(labelResource = "model.fieldNames.directory.targetLocation", order = 1)
    @Column(length = 1204, nullable = false)
    private File targetLocation;

    @Editable(labelResource = "model.fieldNames.directory.favorite", order = 2)
    @Column
    private boolean favorite = false;

    @OrderBy("dateAdded DESC")
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ImageMetaData> imageMetaData;

    @Formula("SELECT COUNT(*) FROM imagemetadata i WHERE i.directory_id = id")
    private long imageMetaDataCount = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(File targetLocation) {
        this.targetLocation = targetLocation;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<ImageMetaData> getImageMetaData() {
        if (imageMetaData == null) {
            imageMetaData = new ArrayList<>();
        }

        return imageMetaData;
    }

    // Used by Dao#load
    public void setImageMetaData(List<ImageMetaData> imageMetaData) {
        this.imageMetaData = imageMetaData;
    }

    // UI properties
    public String getSourceType() {
        return "Local";
    }

    public long getMetaDataCount() {
        if (Hibernate.isInitialized(imageMetaData)){
            return imageMetaData.size();
        } else {
            return imageMetaDataCount;
        }
    }

    public void desktopOpenSource() {
        UIUtils.desktopOpen(getTargetLocation());
    }
}
