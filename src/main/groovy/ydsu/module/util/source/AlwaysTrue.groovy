package ydsu.module.util.source

import java.util.function.Predicate

@Singleton
class AlwaysTrue<T> implements Predicate<T> {
    @Override
    boolean test(T t) {
        true
    }

    @Override
    String toString() {
        'AlwaysTrue{}'
    }
}
