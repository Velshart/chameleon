package me.mmtr.chameleon.exception;

public class ImageNotFoundException extends NotFoundException {
    public ImageNotFoundException(Long id) {
        super("Image not found: " + id);
    }
}
