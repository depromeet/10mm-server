package com.depromeet.domain.image.domain;

import com.depromeet.domain.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    private Long targetId;

    private String imageKey;

    @Enumerated(EnumType.STRING)
    private ImageFileExtension imageFileExtension;

    @Builder(access = AccessLevel.PRIVATE)
    private Image(
            Long id,
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        this.id = id;
        this.imageType = imageType;
        this.targetId = targetId;
        this.imageKey = imageKey;
        this.imageFileExtension = imageFileExtension;
    }

    public static Image createImage(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return Image.builder()
                .imageType(imageType)
                .targetId(targetId)
                .imageKey(imageKey)
                .imageFileExtension(imageFileExtension)
                .build();
    }
}
