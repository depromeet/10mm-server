package com.depromeet.domain.image.dao;

import com.depromeet.domain.image.domain.Image;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.image.domain.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("select i from Image i where i.imageType = :imageType and i.targetId = :targetId and i.imageFileExtension = :imageFileExtension order by i.id desc limit 1")
    Optional<Image> queryImageKey(ImageType imageType, Long targetId, ImageFileExtension imageFileExtension);
}
