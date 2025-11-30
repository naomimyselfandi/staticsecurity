package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.Clearance;

import java.util.UUID;

public interface DocumentRequest extends Clearance {

    UUID getId();

}
