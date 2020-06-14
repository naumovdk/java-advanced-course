package ru.ifmo.rain.naumov.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversedList<E> extends AbstractList<E> {
    private final List<E> data;
    private boolean isReverse;

    ReversedList(List<E> list) {
        if (list instanceof ReversedList) {
            this.data = ((ReversedList<E>) list).data;
            isReverse = !((ReversedList<E>) list).isReverse;
        } else {
            this.data = list;
            isReverse = true;
        }
    }

    @Override
    public E get(int i) {
        return isReverse ? data.get(size() - i -1) : data.get(i);
    }

    @Override
    public int size() {
        return data.size();
    }
}
