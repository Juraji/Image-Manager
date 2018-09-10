package nl.juraji.imagemanager.util.streams;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("unused")
public interface BiStream<K, V>  {

    static <K, V> BiStream<K, V> stream(Map<K, V> map) {
        return stream(map.entrySet().stream());
    }

    static <K, V> BiStream<K, V> stream(Stream<Map.Entry<K, V>> stream) {
        return () -> stream;
    }

    static <K, V> BiStream<K, V> stream(K[] array, Function<? super K, ? extends V> valueMapper) {
        return stream(Arrays.stream(array), valueMapper);
    }

    static <O, K, V> BiStream<K, V> stream(O[] array, Function<? super O, ? extends K> keyMapper, Function<? super O, ? extends V> valueMapper) {
        return stream(Arrays.stream(array), keyMapper, valueMapper);
    }

    static <K, V> BiStream<K, V> stream(Collection<K> collection, Function<? super K, ? extends V> valueMapper) {
        return stream(collection.stream(), valueMapper);
    }

    static <O, K, V> BiStream<K, V> stream(Collection<O> collection, Function<? super O, ? extends K> keyMapper, Function<? super O, ? extends V> valueMapper) {
        return stream(collection.stream(), keyMapper, valueMapper);
    }

    static <K, V> BiStream<K, V> stream(Stream<K> stream, Function<? super K, ? extends V> valueMapper) {
        return () -> stream.map(k -> new AbstractMap.SimpleImmutableEntry<>(k, valueMapper.apply(k)));
    }

    static <O, K, V> BiStream<K, V> stream(Stream<O> stream, Function<? super O, ? extends K> keyMapper, Function<? super O, ? extends V> valueMapper) {
        return () -> stream.map(k -> new AbstractMap.SimpleImmutableEntry<>(keyMapper.apply(k), valueMapper.apply(k)));
    }

    Stream<Map.Entry<K, V>> entries();

    default BiStream<K, V> distinct() {
        return stream(entries().distinct());
    }

    default BiStream<K, V> peek(BiConsumer<? super K, ? super V> biConsumer) {
        return stream(entries().peek(e -> biConsumer.accept(e.getKey(), e.getValue())));
    }

    default BiStream<K, V> skip(long n) {
        return stream(entries().skip(n));
    }

    default BiStream<K, V> limit(long maxSize) {
        return stream(entries().limit(maxSize));
    }

    default BiStream<K, V> filterKey(Predicate<? super K> predicate) {
        return stream(entries().filter(e -> predicate.test(e.getKey())));
    }

    default BiStream<K, V> filterValue(Predicate<? super V> predicate) {
        return stream(entries().filter(e -> predicate.test(e.getValue())));
    }

    default BiStream<K, V> filter(BiPredicate<? super K, ? super V> biPredicate) {
        return stream(entries().filter(e -> biPredicate.test(e.getKey(), e.getValue())));
    }

    default BiStream<V, K> invert() {
        return stream(entries().map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getValue(), e.getKey())));
    }

    default <R> BiStream<R, V> mapKey(Function<? super K, ? extends R> function) {
        return stream(entries().map(e -> new AbstractMap.SimpleImmutableEntry<>(
                function.apply(e.getKey()), e.getValue()
        )));
    }

    default <R> BiStream<R, V> mapKey(BiFunction<? super K, ? super V, ? extends R> biFunction) {
        return stream(entries().map(e -> new AbstractMap.SimpleImmutableEntry<>(
                biFunction.apply(e.getKey(), e.getValue()), e.getValue()
        )));
    }

    default <R> BiStream<K, R> mapValue(Function<? super V, ? extends R> function) {
        return stream(entries().map(e -> new AbstractMap.SimpleImmutableEntry<>(
                e.getKey(), function.apply(e.getValue())
        )));
    }

    default <R> BiStream<K, R> mapValue(BiFunction<? super K, ? super V, ? extends R> biFunction) {
        return stream(entries().map(e -> new AbstractMap.SimpleImmutableEntry<>(
                e.getKey(), biFunction.apply(e.getKey(), e.getValue())
        )));
    }

    default <R> Stream<R> map(BiFunction<? super K, ? super V, ? extends R> biFunction) {
        return entries().map(e -> biFunction.apply(e.getKey(), e.getValue()));
    }

    default DoubleStream mapToDouble(ToDoubleBiFunction<? super K, ? super V> mapper) {
        return entries().mapToDouble(e -> mapper.applyAsDouble(e.getKey(), e.getValue()));
    }

    default IntStream mapToInt(ToIntBiFunction<? super K, ? super V> mapper) {
        return entries().mapToInt(e -> mapper.applyAsInt(e.getKey(), e.getValue()));
    }

    default LongStream mapToLong(ToLongBiFunction<? super K, ? super V> mapper) {
        return entries().mapToLong(e -> mapper.applyAsLong(e.getKey(), e.getValue()));
    }

    default <L, W> BiStream<L, W> flatMap(
            BiFunction<? super K, ? super V, ? extends BiStream<L, W>> biFunction) {
        return stream(entries().flatMap(
                e -> biFunction.apply(e.getKey(), e.getValue()).entries()));
    }

    default <R> Stream<R> flatMapToObj(
            BiFunction<? super K, ? super V, ? extends Stream<R>> mapper) {
        return entries().flatMap(e -> mapper.apply(e.getKey(), e.getValue()));
    }

    default DoubleStream flatMapToDouble(
            BiFunction<? super K, ? super V, ? extends DoubleStream> biFunction) {
        return entries().flatMapToDouble(e -> biFunction.apply(e.getKey(), e.getValue()));
    }

    default IntStream flatMapToInt(
            BiFunction<? super K, ? super V, ? extends IntStream> biFunction) {
        return entries().flatMapToInt(e -> biFunction.apply(e.getKey(), e.getValue()));
    }

    default LongStream flatMapToLong(
            BiFunction<? super K, ? super V, ? extends LongStream> biFunction) {
        return entries().flatMapToLong(e -> biFunction.apply(e.getKey(), e.getValue()));
    }

    default BiStream<K, V> sortedByKey(Comparator<? super K> comparator) {
        return stream(entries().sorted(Map.Entry.comparingByKey(comparator)));
    }

    default BiStream<K, V> sortedByValue(Comparator<? super V> comparator) {
        return stream(entries().sorted(Map.Entry.comparingByValue(comparator)));
    }

    default boolean allMatch(BiPredicate<? super K, ? super V> biPredicate) {
        return entries().allMatch(e -> biPredicate.test(e.getKey(), e.getValue()));
    }

    default boolean anyMatch(BiPredicate<? super K, ? super V> biPredicate) {
        return entries().anyMatch(e -> biPredicate.test(e.getKey(), e.getValue()));
    }

    default boolean noneMatch(BiPredicate<? super K, ? super V> biPredicate) {
        return entries().noneMatch(e -> biPredicate.test(e.getKey(), e.getValue()));
    }

    default long count() {
        return entries().count();
    }

    default Stream<K> keys() {
        return entries().map(Map.Entry::getKey);
    }

    default Stream<V> values() {
        return entries().map(Map.Entry::getValue);
    }

    default Optional<Map.Entry<K, V>> maxByKey(Comparator<? super K> comparator) {
        return entries().max(Map.Entry.comparingByKey(comparator));
    }

    default Optional<Map.Entry<K, V>> maxByValue(Comparator<? super V> comparator) {
        return entries().max(Map.Entry.comparingByValue(comparator));
    }

    default Optional<Map.Entry<K, V>> minByKey(Comparator<? super K> comparator) {
        return entries().min(Map.Entry.comparingByKey(comparator));
    }

    default Optional<Map.Entry<K, V>> minByValue(Comparator<? super V> comparator) {
        return entries().min(Map.Entry.comparingByValue(comparator));
    }

    default void forEach(BiConsumer<? super K, ? super V> biConsumer) {
        entries().forEach(e -> biConsumer.accept(e.getKey(), e.getValue()));
    }

    default void forEachOrdered(BiConsumer<? super K, ? super V> biConsumer) {
        entries().forEachOrdered(e -> biConsumer.accept(e.getKey(), e.getValue()));
    }

    default Map<K, V> toMap() {
        return entries().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    default Map<K, V> toOrderedMap() {
        return entries().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new

        ));
    }
}
