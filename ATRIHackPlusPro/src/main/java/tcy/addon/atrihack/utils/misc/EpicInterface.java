package tcy.addon.atrihack.utils.misc;

@FunctionalInterface
public interface EpicInterface<T, E> {
    E get(T t);
}
