package nextstep.api.subway.domain.operators;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nextstep.api.auth.domain.dto.UserPrincipal;

/**
 * @author : Rene Choi
 * @since : 2024/02/28
 */
class FareCalculatorTest {


	private FareCalculator fareCalculator = new FareCalculator();


	@Test
	@DisplayName("노선 요금을 포함한 총 요금 계산")
	void testCalculateFareWithLineCharges() {
		// Given
		long distance = 25;
		List<Long> lineIds = Arrays.asList(1L, 2L);

		// When
		int fare = fareCalculator.calculateFareWithLineCharges(distance, lineIds);

		// Then
		int expectedFare = 1250 + 100 * 3 + 500;
		assertEquals(expectedFare, fare);
	}

	@ParameterizedTest
	@MethodSource("provideAgesAndExpectedFares")
	@DisplayName("로그인 사용자에 대한 나이별 할인 요금 계산")
	void testCalculateFareWithLineChargesWithAuthUser(long distance, List<Long> lineIds, int age, int expectedFare) {
		// Given
		UserPrincipal userPrincipal = UserPrincipal.builder().age(age).build();

		// When
		int fare = fareCalculator.calculateFareWithLineChargesWithAuthUser(distance, lineIds, userPrincipal);

		// Then
		assertEquals(expectedFare, fare);
	}

	static Stream<Arguments> provideAgesAndExpectedFares() {
		return Stream.of(
			Arguments.of(25, Arrays.asList(1L, 2L), 16, (int) Math.round((1250 + 100 * 3 + 500 - 350) * 0.8)),
			Arguments.of(25, Arrays.asList(1L, 2L), 12, (int) Math.round((1250 + 100 * 3 + 500 - 350) * 0.5)),
			Arguments.of(25, Arrays.asList(1L, 2L), 20, 1250 + 100 * 3 + 500)
		);
	}

	@Test
	@DisplayName("첫 번째 임계 거리 이하일 때 기본 요금 적용")
	void testCalculateFareDistanceLessThanFirstThreshold() {
		// Given
		long distance = 9;

		// When
		int fare = fareCalculator.calculateFare(distance);

		// Then
		assertEquals(1250, fare);
	}

	@Test
	@DisplayName("첫 번째와 두 번째 임계 거리 사이일 때 증가 요금 계산")
	void testCalculateFareDistanceBetweenFirstAndSecondThreshold() {
		// Given
		long distance = 20;

		// When
		int fare = fareCalculator.calculateFare(distance);

		// Then
		int expectedFare = 1250 + 100 * 2;
		assertEquals(expectedFare, fare);
	}

	@Test
	@DisplayName("두 번째 임계 거리 초과일 때 증가 요금 계산")
	void testCalculateFareDistanceMoreThanSecondThreshold() {
		// Given
		long distance = 60;

		// When
		int fare = fareCalculator.calculateFare(distance);

		// Then
		int expectedFare = 1250 + 100 * 8 + 100 * 2;
		assertEquals(expectedFare, fare);
	}
}