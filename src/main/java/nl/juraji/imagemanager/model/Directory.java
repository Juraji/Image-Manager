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
public class Directory implements TileData {

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

    @Editable(labelResource = "model.fieldNames.directory.ignored", order = 3)
    @Column
    private boolean ignored = false;

    @OrderBy("dateAdded DESC")
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageMetaData> imageMetaData;

    @ManyToOne(fetch = FetchType.LAZY)
    private Directory parent;

    @OrderBy("name ASC")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Directory> directories;

    @Formula("SELECT COUNT(*) FROM imagemetadata i WHERE i.directory_id = id")
    private long imageMetaDataCount = 0;

    @Formula("SELECT COUNT(*) FROM directory d WHERE d.parent_id = id")
    private long subDirectoryCount = 0;

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

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public List<ImageMetaData> getImageMetaData() {
        if (imageMetaData == null) {
            imageMetaData = new ArrayList<>();
        }

        return imageMetaData;
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    public List<Directory> getDirectories() {
        if (directories == null) {
            directories = new ArrayList<>();
        }

        return directories;
    }

    // UI properties
    public String getSourceType() {
        return "Local";
    }

    public long getMetaDataCount() {
        if (Hibernate.isInitialized(imageMetaData)) {
            return getImageMetaData().size();
        } else {
            return imageMetaDataCount;
        }
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public long getSubDirectoryCount() {
        if (Hibernate.isInitialized(directories)) {
            return getDirectories().size();
        } else {
            return subDirectoryCount;
        }
    }

    public void desktopOpenSource() {
        UIUtils.desktopOpen(getTargetLocation());
    }
}
