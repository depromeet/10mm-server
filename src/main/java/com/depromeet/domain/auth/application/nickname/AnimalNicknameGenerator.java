package com.depromeet.domain.auth.application.nickname;

import com.depromeet.global.common.constants.NicknameGenerationConstants;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class AnimalNicknameGenerator implements NicknameGenerationStrategy {

    @Override
    public String generate() {
        int animalIndex =
                ThreadLocalRandom.current()
                        .nextInt(NicknameGenerationConstants.ANIMAL_NAMES.length);
        int prefixIndex =
                ThreadLocalRandom.current()
                        .nextInt(NicknameGenerationConstants.PREFIX_NAMES.length);

        String animalName = NicknameGenerationConstants.ANIMAL_NAMES[animalIndex];
        String prefix = NicknameGenerationConstants.PREFIX_NAMES[prefixIndex];

        return prefix + animalName;
    }
}
