package com.depromeet.domain.mission.domain;

import java.time.Period;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionPeriod {
    TWO_WEEKS("2주", Period.ofWeeks(2)),
    ONE_MONTH("1개월", Period.ofMonths(1)),
    TWO_MONTHS("2개월", Period.ofMonths(2)),
    THREE_MONTHS("3개월", Period.ofMonths(3)),
    FOUR_MONTHS("4개월", Period.ofMonths(4)),
    ;

    private final String value;
    private final Period period;
}
