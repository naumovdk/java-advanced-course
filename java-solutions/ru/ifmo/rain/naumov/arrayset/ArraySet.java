package ru.ifmo.rain.naumov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> data;
    private Comparator<? super E> comparator;

    public ArraySet() {
        this.data = Collections.emptyList();
        this.comparator = null;
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this.data = Collections.emptyList();
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        var s = new TreeSet<E>(comparator);
        s.addAll(collection);
        this.data = List.copyOf(s);
        this.comparator = comparator;
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.data = list;
        this.comparator = comparator;
    }

    private E get(int index) {
        return index < 0 || index >= size() ? null : data.get(index);
    }

    private int find(E e, int inclusive, int lower) {
        int pos = Collections.binarySearch(data, e, comparator);
        return (pos >= 0 ? pos + inclusive : ~pos + lower);
    }

    private E findLower(E e, boolean inclusive) {
        return get(find(e, inclusive ? 0 : -1, -1));
    }

    private E findUpper(E t, boolean inclusive) {
        return get(find(t, inclusive ? 0 : 1, 0));
    }

    @Override
    public E lower(E e) {
        return findLower(e, false);
    }

    @Override
    public E floor(E e) {
        return findLower(e, true);
    }

    @Override
    public E ceiling(E e) {
        return findUpper(e, true);
    }

    @Override
    public E higher(E e) {
        return findUpper(e, false);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,E toElement, boolean toInclusive) {
        int left = find(fromElement, fromInclusive ? 0 : 1, 0);
        int right = find(toElement, toInclusive ? 0 : -1, -1) + 1;
        return new ArraySet<>((right <= left ? Collections.emptyList() : data.subList(left, right)), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @Override
    @SuppressWarnings("uncheked cast")
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (comparator == null ? ((Comparable<E>) fromElement).compareTo(toElement) > 0
                : comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        if (isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public E last() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, o, (Comparator<Object>) comparator) >= 0;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}