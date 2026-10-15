package com.company.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class MemberBusinessRulesTest {

    @Test
    void memberId_isGeneratedAutomatically() throws NoSuchFieldException {
        Field memberId = Member.class.getDeclaredField("memberId");

        assertThat(memberId.isAnnotationPresent(Id.class)).isTrue();
        assertThat(memberId.getAnnotation(GeneratedValue.class).strategy())
                .isEqualTo(GenerationType.IDENTITY);
    }

    @Test
    void memberEmail_hasUniqueAndMandatoryDatabaseConstraints() throws NoSuchFieldException {
        Field email = Member.class.getDeclaredField("email");
        Column column = email.getAnnotation(Column.class);

        assertThat(column).isNotNull();
        assertThat(column.nullable()).isFalse();
        assertThat(column.unique()).isTrue();
    }

    @Test
    void membershipTier_containsOnlySupportedValues() {
        assertThat(MembershipTier.values())
                .containsExactly(
                        MembershipTier.STANDARD,
                        MembershipTier.SILVER,
                        MembershipTier.GOLD,
                        MembershipTier.PLATINUM
                );
    }

    @Test
    void newNightBalance_startsWithFiveAvailableNights() {
        NightBalance nightBalance = new NightBalance();

        assertThat(nightBalance.getAvailableNights()).isEqualTo(5);
    }
}
