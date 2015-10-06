package javaslang.collection;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Map.Entry;
import javaslang.control.Some;
import org.assertj.core.api.IterableAssert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static javaslang.Serializables.deserialize;
import static javaslang.Serializables.serialize;

public abstract class AbstractMapTest extends AbstractTraversableTest {

    @Override
    protected <T> IterableAssert<T> assertThat(java.lang.Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @Override
            public IterableAssert<T> isEqualTo(Object obj) {
                @SuppressWarnings("unchecked")
                java.lang.Iterable<T> expected = (java.lang.Iterable<T>) obj;
                java.util.Map<T, Integer> actualMap = countMap(actual);
                java.util.Map<T, Integer> expectedMap = countMap(expected);
                assertThat(actualMap.toString()).isEqualTo(expectedMap.toString());
                return this;
            }

            private java.util.Map<T, Integer> countMap(java.lang.Iterable<? extends T> it) {
                java.util.HashMap<T, Integer> cnt = new java.util.HashMap<>();
                it.forEach(i -> cnt.merge(i, 1, (v1, v2) -> v1 + v2));
                return cnt;
            }
        };
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, IntMap<T>> collector() {
        final Collector<Entry<Integer, T>, ArrayList<Entry<Integer, T>>, ? extends Map<Integer, T>> mapCollector = mapCollector();
        return new Collector<T, ArrayList<T>, IntMap<T>>() {
            @Override
            public Supplier<ArrayList<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<ArrayList<T>, T> accumulator() {
                return ArrayList::add;
            }

            @Override
            public BinaryOperator<ArrayList<T>> combiner() {
                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };
            }

            @Override
            public Function<ArrayList<T>, IntMap<T>> finisher() {
                return AbstractMapTest.this::ofAll;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return mapCollector.characteristics();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> IntMap<T> empty() {
        return IntMap.of(emptyMap());
    }

    private <T> Map<Integer, T> emptyInt() {
        return emptyMap();
    }

    private Map<Integer, Integer> emptyIntInt() {
        return emptyMap();
    }

    abstract protected String className();

    abstract protected <T1, T2> Map<T1, T2> emptyMap();

    abstract protected <T> Collector<Entry<Integer, T>, ArrayList<Entry<Integer, T>>, ? extends Map<Integer, T>> mapCollector();

    @SuppressWarnings("unchecked")
    abstract protected <K, V> Map<K, V> mapOf(Entry<? extends K, ? extends V>... entries);

    @Override
    boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> IntMap<T> of(T element) {
        Map<Integer, T> map = emptyMap();
        map = map.put(0, element);
        return IntMap.of(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> IntMap<T> of(T... elements) {
        Map<Integer, T> map = emptyMap();
        for (T element : elements) {
            map = map.put(map.size(), element);
        }
        return IntMap.of(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> IntMap<T> ofAll(Iterable<? extends T> elements) {
        Map<Integer, T> map = emptyMap();
        for (T element : elements) {
            map = map.put(map.size(), element);
        }
        return IntMap.of(map);
    }

    @Override
    protected IntMap<Boolean> ofAll(boolean[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Byte> ofAll(byte[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Character> ofAll(char[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Double> ofAll(double[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Float> ofAll(float[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Integer> ofAll(int[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Long> ofAll(long[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    @Override
    protected IntMap<Short> ofAll(short[] array) {
        return ofAll(Iterator.ofAll(array));
    }

    // - toString

    @Test
    public void shouldMakeString() {
        assertThat(emptyMap().toString()).isEqualTo(className() + "()");
        assertThat(emptyMap().put(1, 2).toString()).isEqualTo(className() + "(" + Entry.of(1, 2) + ")");
    }

    // -- apply

    @Test
    public void shouldApplyExistingKey() {
        assertThat(emptyMap().put(1, 2).apply(1)).isEqualTo(2);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldApplyNonExistingKey() {
        emptyMap().put(1, 2).apply(3);
    }

    // -- contains

    @Test
    public void shouldFindKey() {
        assertThat(emptyMap().put(1, 2).containsKey(1)).isTrue();
        assertThat(emptyMap().put(1, 2).containsKey(2)).isFalse();
    }

    @Test
    public void shouldFindValue() {
        assertThat(emptyMap().put(1, 2).containsValue(2)).isTrue();
        assertThat(emptyMap().put(1, 2).containsValue(1)).isFalse();
    }

    // -- flatMap

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFlatMapUsingBiFunction() {
        final Map<Integer, Integer> testee = mapOf(Entry.of(1, 11), Entry.of(2, 22), Entry.of(3, 33));
        final Map<String, String> actual = testee.flatMap((k, v) -> List.of(Entry.of(String.valueOf(k), String.valueOf(v)), Entry.of(String.valueOf(k * 10), String.valueOf(v * 10))));
        final Map<String, String> expected = mapOf(Entry.of("1", "11"), Entry.of("10", "110"), Entry.of("2", "22"), Entry.of("20", "220"), Entry.of("3", "33"), Entry.of("30", "330"));
        assertThat(actual).isEqualTo(expected);
    }

    // -- map

    @Test
    public void shouldMapEmpty() {
        assertThat(emptyInt().map(Entry::key)).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldMapNonEmpty() {
        final javaslang.collection.Seq<Integer> expected = Vector.of(1, 2);
        final javaslang.collection.Seq<Integer> actual = emptyInt().put(1, "1").put(2, "2").map(Entry::key);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptySetWhenAskedForEntrySetOfAnEmptyMap() {
        assertThat(emptyMap().entrySet()).isEqualTo(HashSet.empty());
    }

    @Test
    public void shouldReturnEntrySetOfANonEmptyMap() {
        assertThat(emptyMap().put(1, "1").put(2, "2").entrySet()).isEqualTo(
                HashSet.of(Entry.of(1, "1"), Entry.of(2, "2")));
    }

    // -- equality

    @Test
    public void shouldIgnoreOrderOfEntriesWhenComparingForEquality() {
        final Map<?, ?> map1 = emptyMap().put(1, 'a').put(2, 'b').put(3, 'c');
        final Map<?, ?> map2 = emptyMap().put(3, 'c').put(2, 'b').put(1, 'a').remove(2).put(2, 'b');
        assertThat(map1).isEqualTo(map2);
    }

    // -- removeAll

    @Test
    public void shouldRmoveAll() {
        assertThat(emptyMap().put(1, 'a').put(2, 'b').put(3, 'c').removeAll(List.of(1, 3))).isEqualTo(emptyMap().put(2, 'b'));
    }

    // -- unzip

    @Test
    public void shouldUnzipNil() {
        assertThat(emptyMap().unzip(x -> Tuple.of(x, x))).isEqualTo(Tuple.of(Stream.empty(), Stream.empty()));
        assertThat(emptyMap().unzip((k, v) -> Tuple.of(Entry.of(k, v), Entry.of(k, v)))).isEqualTo(Tuple.of(Stream.empty(), Stream.empty()));
    }

    @Test
    public void shouldUnzipNonNil() {
        Map<Integer, Integer> map = emptyIntInt().put(0, 0).put(1, 1);
        final Tuple actual = map.unzip(entry -> Tuple.of(entry.key, entry.value + 1));
        final Tuple expected = Tuple.of(Stream.of(0, 1), Stream.of(1, 2));
        assertThat(actual).isEqualTo(expected);
    }

    // -- zip

    @Test
    public void shouldZipNils() {
        final Seq<Tuple2<Entry<Object, Object>, Object>> actual = emptyMap().zip(List.empty());
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipEmptyAndNonNil() {
        final Seq<Tuple2<Entry<Object, Object>, Integer>> actual = emptyMap().zip(List.of(1));
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonEmptyAndNil() {
        final Seq<Tuple2<Entry<Integer, Integer>, Object>> actual = emptyIntInt().put(0, 1).zip(List.empty());
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final Seq<Tuple2<Entry<Integer, Integer>, Integer>> actual = emptyIntInt().put(0, 0).put(1, 1).zip(List.of(5, 6, 7));
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Entry.of(0, 0), 5), Tuple.of(Entry.of(1, 1), 6)));
    }

    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final Seq<Tuple2<Entry<Integer, Integer>, Integer>> actual = emptyIntInt().put(0, 0).put(1, 1).put(2, 2).zip(List.of(5, 6));
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Entry.of(0, 0), 5), Tuple.of(Entry.of(1, 1), 6)));
    }

    @Test
    public void shouldZipNonNilsOfSameSize() {
        final Seq<Tuple2<Entry<Integer, Integer>, Integer>> actual = emptyIntInt().put(0, 0).put(1, 1).put(2, 2).zip(List.of(5, 6, 7));
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Entry.of(0, 0), 5), Tuple.of(Entry.of(1, 1), 6), Tuple.of(Entry.of(2, 2), 7)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipWithThatIsNull() {
        emptyMap().zip(null);
    }

    // -- zipWithIndex

    @Test
    public void shouldZipNilWithIndex() {
        assertThat(emptyMap().zipWithIndex()).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonNilWithIndex() {
        final Seq<Tuple2<Entry<Integer, Integer>, Integer>> actual = emptyIntInt().put(0, 0).put(1, 1).put(2, 2).zipWithIndex();
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Entry.of(0, 0), 0), Tuple.of(Entry.of(1, 1), 1), Tuple.of(Entry.of(2, 2), 2)));
    }

    // -- zipAll

    @Test
    public void shouldZipAllNils() {
        final Seq<Tuple2<Entry<Object, Object>, Object>> actual = emptyMap().zipAll(empty(), null, null);
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipAllEmptyAndNonNil() {
        final Seq<Tuple2<Entry<Object, Object>, Object>> actual = emptyMap().zipAll(List.of(1), null, null);
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(null, 1)));
    }

    @Test
    public void shouldZipAllNonEmptyAndNil() {
        final Seq<Tuple2<Entry<Object, Object>, Object>> actual = emptyMap().put(0, 1).zipAll(empty(), null, null);
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Entry.of(0, 1), null)));
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final Seq<Tuple2<Entry<Object, Object>, String>> actual = emptyMap().put(1, 1).put(2, 2).zipAll(of("a", "b", "c"), Entry.of(9, 10), "z");
        final Seq<Tuple2<Entry<Object, Object>, String>> expected = Stream.of(Tuple.of(Entry.of(1, 1), "a"), Tuple.of(Entry.of(2, 2), "b"), Tuple.of(Entry.of(9, 10), "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsMoreSmaller() {
        final Seq<Tuple2<Entry<Object, Object>, String>> actual = emptyMap().put(1, 1).put(2, 2).zipAll(of("a", "b", "c", "d"), Entry.of(9, 10), "z");
        final Seq<Tuple2<Entry<Object, Object>, String>> expected = Stream.of(Tuple.of(Entry.of(1, 1), "a"), Tuple.of(Entry.of(2, 2), "b"), Tuple.of(Entry.of(9, 10), "c"), Tuple.of(Entry.of(9, 10), "d"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final Seq<Tuple2<Entry<Object, Object>, String>> actual = emptyMap().put(1, 1).put(2, 2).put(3, 3).zipAll(of("a", "b"), Entry.of(9, 10), "z");
        final Seq<Tuple2<Entry<Object, Object>, String>> expected = Stream.of(Tuple.of(Entry.of(1, 1), "a"), Tuple.of(Entry.of(2, 2), "b"), Tuple.of(Entry.of(3, 3), "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsMoreSmaller() {
        final Seq<Tuple2<Entry<Object, Object>, String>> actual = emptyMap().put(1, 1).put(2, 2).put(3, 3).put(4, 4).zipAll(of("a", "b"), Entry.of(9, 10), "z");
        final Seq<Tuple2<Entry<Object, Object>, String>> expected = Stream.of(Tuple.of(Entry.of(1, 1), "a"), Tuple.of(Entry.of(2, 2), "b"), Tuple.of(Entry.of(3, 3), "z"), Tuple.of(Entry.of(4, 4), "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final Seq<Tuple2<Entry<Object, Object>, String>> actual = emptyMap().put(1, 1).put(2, 2).put(3, 3).zipAll(of("a", "b", "c"), Entry.of(9, 10), "z");
        final Seq<Tuple2<Entry<Object, Object>, String>> expected = Stream.of(Tuple.of(Entry.of(1, 1), "a"), Tuple.of(Entry.of(2, 2), "b"), Tuple.of(Entry.of(3, 3), "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipAllWithThatIsNull() {
        emptyMap().zipAll(null, null, null);
    }

    // -- special cases

    @Override
    public void shouldComputeDistinctOfNonEmptyTraversable() {
        final Map<Object, Object> testee = emptyMap().put(1, 1).put(2, 2).put(3, 3);
        assertThat(testee.distinct()).isEqualTo(testee);
    }

    @Override
    public void shouldReturnSomeTailWhenCallingTailOptionOnNonNil() {
        assertThat(of(1, 2, 3).tailOption().get()).isEqualTo(new Some<>(of(2, 3)).get());
    }

    @Override
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        IntMap<?> obj = deserialize(serialize(empty()));
        final boolean actual = obj.original() == empty().original();
        assertThat(actual).isTrue();
    }

    @Override
    public void shouldFoldRightNonNil() {
        final String actual = of('a', 'b', 'c').foldRight("", (x, xs) -> x + xs);
        final List<String> expected = List.of('a', 'b', 'c').permutations().map(List::mkString);
        assertThat(actual).isIn(expected);
    }
}