package io.github.naomimyselfandi.staticsecurityintegration;

import java.util.Optional;

public interface DocumentUpdateRequest extends DocumentRequest {

    String getContents();

    Optional<Integer> getChapter();

    default boolean createsNewChapter() {
        return getChapter().isEmpty();
    }

}
