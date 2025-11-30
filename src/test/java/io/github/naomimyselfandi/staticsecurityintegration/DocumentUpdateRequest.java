package io.github.naomimyselfandi.staticsecurityintegration;

import java.util.OptionalInt;

public interface DocumentUpdateRequest extends DocumentRequest {

    String getContents();

    OptionalInt getChapter();

    default boolean createsNewChapter() {
        return getChapter().isEmpty();
    }

}
