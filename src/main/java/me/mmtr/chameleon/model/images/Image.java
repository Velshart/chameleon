package me.mmtr.chameleon.model.images;

import jakarta.persistence.*;
import me.mmtr.chameleon.model.User;

import java.time.Instant;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "image_seq"
    )
    @SequenceGenerator(
            name = "image_seq",
            sequenceName = "image_id_seq",
            allocationSize = 100
    )
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Image() {}

    public Image(
            String originalFilename,
            String contentType,
            long size,
            User owner
    ) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.uploadedAt = Instant.now();
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public User getOwner() {
        return owner;
    }
}
