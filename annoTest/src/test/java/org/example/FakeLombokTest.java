package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FakeLombokTest {
    // given
    Car car1 = new Car();

    @Test
    @DisplayName("getter 메소드 테스트")
    void testGetter(){

        //when
        String name = car1.getName();
        String company = car1.getCompany();

        //then
        assertThat(name).isEqualTo("로드스터 2");
        assertThat(company).isEqualTo("테슬라");
    }

    @Test
    @DisplayName("setter 메소드 테스트")
    void testSetter(){

        //when
        car1.setName("소나타");
        car1.setCompany("현대");
        String name = car1.getName();
        String company = car1.getCompany();

        //then
        assertThat(name).isEqualTo("소나타");
        assertThat(company).isEqualTo("현대");
    }


}