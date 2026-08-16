package me.mmtr.chameleon.repository;

import me.mmtr.chameleon.model.images.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDataRepository extends JpaRepository<ImageData, Long> {}
