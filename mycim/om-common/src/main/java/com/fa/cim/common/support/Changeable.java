package com.fa.cim.common.support;

import lombok.Getter;
import lombok.Setter;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * description:
 * <p>Changeable .</p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/3/1        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2019/3/1 15:07
 * @copyright: 2019, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
public final class Changeable<T> {

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * status
     */
    @Getter
    @Setter
    private Status status = Status.NORMAL;

    private Changeable() {
        this.value = null;
    }

    private Changeable(T value) {
        this.value = Objects.requireNonNull(value);
    }

    private Changeable(T value, Status status) {
        this.value = Objects.requireNonNull(value);
        this.status = status;
    }

    public static <T> Changeable<T> of(T value) {
        return new Changeable<>(value);
    }

    public static <T> Changeable<T> of(T value, Status status) {
        return new Changeable<>(value, status);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Changeable)) {
            return false;
        }

        Changeable<?> other = (Changeable<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Changeable[%s]", value)
                : "Changeable.empty";
    }

    public enum Status {
        /**
         * do nothing
         */
        NORMAL,
        /**
         * insert or update
         */
        CHANGED,
        /**
         * delete
         */
        DELETED;
    }
}
