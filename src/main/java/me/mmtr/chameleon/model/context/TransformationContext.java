package me.mmtr.chameleon.model.context;

import java.awt.image.BufferedImage;

public final class TransformationContext {

    private BufferedImage image;
    private String outputFormat = "png";
    private float quality = 1.0f;

    public TransformationContext(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }
}
