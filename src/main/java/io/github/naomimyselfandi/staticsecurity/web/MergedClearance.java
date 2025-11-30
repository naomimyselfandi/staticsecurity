package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.Unwrap;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Merge a clearance object from multiple parts of a request. Merging uses an
 * intermediate object (typically a record) which is instantiated from a request
 * and then converted to a clearance object. Standard web annotations such as
 * {@link PathVariable}, {@link RequestParam}, and {@link RequestBody} may be
 * placed on the intermediate type's constructor, specifying how the request
 * should be interpreted. {@link RequestBody} is often used alongside this
 * library's {@link Unwrap} annotation to allow all properties that are not
 * explicitly mapped to be extracted from the request body.
 *
 * <p>An example may be informative:
 *
 * <pre>{@code
 * record ExampleDefinition(
 *     @PathVariable Long id,
 *     @RequestParam(required = false, name = "foo") Boolean bar,
 *     @RequestBody @Unwrap Map<String, Object> body
 * ) {}
 *
 * @GetMapping("/{id}")
 * ResponseEntity<?> exampleMethod(
 *     @MergedClearance(ExampleDefinition.class) ExampleClearance clearance
 * ) {
 *   return exampleService.get(clearance);
 * }
 * }</pre>
 * </p>
 *
 * <p>This example maps the {@code ExampleClearance}'s {@code id} property to a
 * path variable with the same name, its {@code} bar property to an optional
 * request parameter named {@code foo}, and all other properties to the request
 * body. (If the {@code foo} parameter is not given, the default specified in
 * {@code ExampleClearance} is used.)</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MergedClearance {

    /**
     * Specify the merge definition type.
     * @return The merge definition type.
     * @see MergedClearance The class-level documentation has more details.
     */
    Class<?> value();

}
