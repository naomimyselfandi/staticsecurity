package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSourceCacheTest {

    private interface Source {}

    private interface TestClearance extends Clearance {}

    @Mock
    private Property property1, property2;

    @Mock
    private PropertyProvider<Source> provider;

    @Mock
    private Cache<Class<?>, List<Property>> propertyCache;

    @Mock
    private Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    private DataSourceCache fixture;

    @BeforeEach
    void setup() {
        doReturn(provider).when(propertyProviderCache).get(Source.class);
        when(propertyCache.get(TestClearance.class)).thenReturn(List.of(property1, property2));
        lenient().when(property1.name()).thenReturn(UUID.randomUUID().toString());
        lenient().when(property2.name()).thenReturn(UUID.randomUUID().toString());
        fixture = new DataSourceCache(propertyCache, propertyProviderCache);
    }

    @RepeatedTest(6)
    void calculate_Extract(RepetitionInfo repetitionInfo) {
        Property p1, p2;
        var repetition = repetitionInfo.getCurrentRepetition() - 1;
        if ((repetition & 1) == 1) {
            p1 = property1;
            p2 = property2;
        } else {
            p1 = property2;
            p2 = property1;
        }
        when(p1.required()).thenReturn(true);
        when(p2.required()).thenReturn(repetition >= 2);
        when(provider.canExtract(p1)).thenReturn(true);
        lenient().when(provider.canExtract(p1)).thenReturn(true);
        lenient().when(provider.canExtract(p2)).thenReturn(repetition >= 2);
        var expected = new ExtractingDataSource<>(provider, List.of(property1, property2));
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

    @RepeatedTest(2)
    void calculate_Extract_WhenARequiredPropertyIsUnavailable_ThenReturnsNothing(RepetitionInfo repetitionInfo) {
        if (repetitionInfo.getCurrentRepetition() == 1) {
            lenient().when(property1.required()).thenReturn(true);
            lenient().when(property2.required()).thenReturn(false);
            lenient().when(provider.canExtract(property1)).thenReturn(false);
            lenient().when(provider.canExtract(property2)).thenReturn(true);
        } else {
            lenient().when(property1.required()).thenReturn(false);
            lenient().when(property2.required()).thenReturn(true);
            lenient().when(provider.canExtract(property1)).thenReturn(true);
            lenient().when(provider.canExtract(property2)).thenReturn(false);
        }
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class))).isEmpty();
    }

    @RepeatedTest(4)
    void calculate_Flatten(RepetitionInfo repetitionInfo) {
        var repetition = repetitionInfo.getCurrentRepetition() - 1;
        var requiredProperty = ((repetition & 1) == 1) ? property1 : property2;
        var optionalProperty = ((repetition & 1) != 1) ? property1 : property2;
        when(requiredProperty.required()).thenReturn(true);
        when(optionalProperty.required()).thenReturn(false);
        when(provider.canFlatten(requiredProperty)).thenReturn(true);
        lenient().when(provider.canFlatten(optionalProperty)).thenReturn(repetition > 2);
        var expected = new FlatteningDataSource<>(provider, requiredProperty);
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

    @RepeatedTest(4)
    void calculate_Flatten_WhenThePropertyIsUnavailable_ThenDoesNotCreateAnExtractor(RepetitionInfo repetitionInfo) {
        var repetition = repetitionInfo.getCurrentRepetition() - 1;
        var requiredProperty = ((repetition & 1) == 1) ? property1 : property2;
        var optionalProperty = ((repetition & 1) != 1) ? property1 : property2;
        when(requiredProperty.required()).thenReturn(true);
        when(optionalProperty.required()).thenReturn(false);
        when(provider.canFlatten(requiredProperty)).thenReturn(false);
        lenient().when(provider.canFlatten(optionalProperty)).thenReturn(repetition > 2);
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class))).isEmpty();
    }

    @RepeatedTest(2)
    void calculate_Pair(RepetitionInfo repetitionInfo) {
        var requiredProperty = (repetitionInfo.getCurrentRepetition() == 1) ? property1 : property2;
        var optionalProperty = (repetitionInfo.getCurrentRepetition() == 1) ? property2 : property1;
        when(requiredProperty.required()).thenReturn(true);
        when(optionalProperty.required()).thenReturn(false);
        when(provider.canExtract(any())).thenReturn(true);
        when(provider.canFlatten(any())).thenReturn(true);
        var expected = new DataSourcePair<>(
                new ExtractingDataSource<>(provider, List.of(property1, property2)),
                new FlatteningDataSource<>(provider, requiredProperty)
        );
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

}
