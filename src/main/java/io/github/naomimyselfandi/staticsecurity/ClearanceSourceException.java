package io.github.naomimyselfandi.staticsecurity;

import lombok.experimental.StandardException;

/**
 * An exception thrown to indicate a clearance object could not be created. This
 * exception does not indicate that access was denied, but that access could not
 * be checked because the source object did not provide a value for at least one
 * of the clearance type's required properties.
 */
@StandardException
public class ClearanceSourceException extends RuntimeException {}
