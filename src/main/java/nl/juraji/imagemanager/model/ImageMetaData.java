package nl.juraji.imagemanager.model;

import javax.persistence.*;
import java.io.File;
import java.time.LocalDateTime;
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

    @OneToOne(mappedBy = "imageMetaData", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageMetaData)) return false;
        ImageMetaData metaData = (ImageMetaData) o;
        return Objects.equals(getId(), metaData.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null){
            return super.hashCode();
        }
        return Objects.hash(getId());
    }
}
