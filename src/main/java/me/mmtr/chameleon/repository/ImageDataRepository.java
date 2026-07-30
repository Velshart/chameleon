package me.mmtr.chameleon.repository;

import me.mmtr.chameleon.model.images.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageDataRepository extends JpaRepository<ImageData, Long> {}
