package me.mmtr.chameleon.repository;

import me.mmtr.chameleon.model.images.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByIdAndOwnerUsername(Long id, String username);

    Page<Image> findAllByOwnerUsername(
            String username,
            Pageable pageable
    );
}
