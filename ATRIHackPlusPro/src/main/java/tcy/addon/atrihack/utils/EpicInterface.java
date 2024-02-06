package tcy.addon.atrihack.utils;

@FunctionalInterface
public interface EpicInterface<T, E> {
    E get(T t);
}
