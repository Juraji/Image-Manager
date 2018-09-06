package nl.juraji.imagemanager.model;

import nl.juraji.imagemanager.util.ui.modelfields.Editable;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ImageMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @OneToOne(mappedBy = "imageMetaData", cascade = CascadeType.ALL, orphanRemoval = true)
    private ImageHash imageHash;

    @Column(length = 1024, nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Directory directory;

    @Column(nullable = false)
    private long qualityRating = 0;

    @Column(nullable = false)
    private int imageWidth = 0;

    @Column(nullable = false)
    private int imageHeight = 0;

    @Column(nullable = false)
    private long fileSize = 0;

    @Column
    private LocalDateTime dateAdded;

    @Editable(labelResource = "model.fieldNames.imageMetaData.description", order = 0, textArea = true, nullable = true)
    @Column(length = 2048)
    private String description;

    @Editable(labelResource = "model.fieldNames.imageMetaData.tags", order = 1, textArea = true, nullable = true)
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> tags;

    @Formula("SELECT d.name FROM directory d WHERE d.id = directory_id")
    private String directoryName = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ImageHash getImageHash() {
        return imageHash;
    }

    public void setImageHash(ImageHash imageHash) {
        this.imageHash = imageHash;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public long getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(long qualityRating) {
        this.qualityRating = qualityRating;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long imageSizeBytes) {
        this.fileSize = imageSizeBytes;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageMetaData)) return false;
        ImageMetaData metaData = (ImageMetaData) o;
        return Objects.equals(getId(), metaData.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        return Objects.hash(getId());
    }
}
