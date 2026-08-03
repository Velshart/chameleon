package me.mmtr.chameleon.model.images;

import jakarta.persistence.*;

@Entity
@Table(name = "image_data")
public class ImageData {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Image image;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    public ImageData() {}

    public ImageData(Image image, byte[] data) {
        this.image = image;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Image getImage() {
        return image;
    }

    public Long getId() {
        return id;
    }
}
