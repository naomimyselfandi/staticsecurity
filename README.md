# StaticSecurity

> Statically checked, declarative access controls for Spring applications.

## Overview

Permission checks are typically a runtime construct. An application may check
permissions in its controllers, in its services, or both, with each approach
having its own pros and cons. This library provides a third option, where
permissions are validated statically. Each type of permission is represented as
a *clearance type,* an ordinary interface that extends `Clearance`. This library
provides several ways to obtain clearances, all of which apply permission checks
first. Sensitive methods simply declare parameters of these types, and ordinary
type-checking verifies that each call to them has had appropriate permission
checks applied. In effect, the permissions required to call a method become part
of its signature.

## Setup

To enable this library, place `@EnableStaticSecurity` on a Spring configuration
class, or use Spring Boot's automatic configuration. All library features whose
dependencies are available are automatically enabled.

## Defining Clearances

Clearance types are interfaces that extend the `Clearance` interface. Each type
of clearance represents some kind of operation that a user may be authorized to
perform, and defines whatever properties are needed to represent the operation.
Consider the following examples from a hypothetical document management system:

```java
public interface DocumentClearance extends Clearance {

    UUID getId();

}

public interface DocumentUpdateClearance extends DocumentClearance {}

public interface DocumentEditClearance extends DocumentUpdateClearance {

    String getContents();

    OptionalInt getChapter();

    default boolean newChapter() {
        return getChapter().isEmpty();
    }

}
```

`DocumentClearance` is a base type for all operations that involve a specific
document. By convention, the base of a clearance hierarchy is also used for
basic read operations.

`DocumentUpdateClearance` is a base type for all operations that update a single
document. It doesn't add any new properties, but serves a purely organizational
function. `DocumentEditClearance` is a specialization for a specific type of
update. It has four properties - `id` (inherited from `DocumentClearance`),
`contents`, `chapter`, and `newChapter` - of which two are required and
two are optional.

> The following return types make a property optional: `OptionalInt`,
> `OptionalLong`, `OptionalDouble`, and `Optional<T>` for any `T`. `default`
> methods also define optional properties, regardless of the return type.

These interfaces have all the information an access policy could need to make
a decision. They also have all the information needed to actually execute the
request. This is a common and recommended pattern, because it allows clearances
to be used as request DTOs (and this library has explicit support for it), but
how an application configures its clearances is ultimately up to individual
discretion.

Clearances can be nested, which is useful when an operation touches multiple
entities:

```java
public interface DocumentLinkClearance extends DocumentUpdateClearance {

    DocumentClearance getTarget();

    String getRelation();

}
```

The above definition ensures that a user can only link one document to another
if they can update one document and see the other.

Since clearance types are interfaces, they can be composed freely. For example,
a clearance for administrative operations could be used as an alternative to
`hasRole`. Any operation which is limited to admins simply extends it and any
other clearance types it should extend, and declares new properties as needed.

## Defining Access Policies

An access policy is simply a Spring bean that implements `AccessPolicy`. Each
access policy applies to some clearance type. Policies are covariant, and more
general policies are applied before more specific ones.

Consider the following examples:

```java
@Component
class DocumentAccessPolicy implements AccessPolicy<DocumentClearance> {

    @Override
    public @Nullable Denial apply(DocumentClearance request) {
        var id = request.getDocumentId();
        var user = Clearance.getAuthentication(request);
        if (documentExistsAndIsVisibleTo(id, user)) {
            return null;
        } else {
            return () -> new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}

@Component
class DocumentUpdatePolicy implements AccessPolicy<DocumentUpdateClearance> {

    @Override
    public @Nullable Denial apply(DocumentUpdateClearance request) {
        var id = request.getDocumentId();
        var user = Intent.getAuthentication(request);
        if (!canEdit(id, user)) {
            return () -> new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else if (isLocked(id)) {
            return () -> new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            return null;
        }
    }

}
```

Since `DocumentUpdateClearance` extends `DocumentClearance`, the
`DocumentAccessPolicy` is applied first. Even if document IDs are sensitive, the
`DocumentUpdatePolicy` can return detailed errors without any risk of confirming
that an ID exists.

This example uses the `ResponseStatusException` from Spring Web. Applications
may use a domain-specific exception if they'd prefer, especially if they do not
use Spring Web.

## Issuing Clearances

The most basic means of issuing a clearance is the `StaticSecurityService`,
which accepts a *source object* and maps it to the requested clearance. Using
this service directly is rare, and other means of issuing clearances are more
often used because they offer various advantages, but since they're built on top
of this service, understanding how it works is still valuable.

Typically, source objects are mapped to clearance objects reflectively, but
other mapping strategies may be defined by implementing the `PropertyProvider`
SPI. Consider the `DocumentEditClearance` interface from above. An object with
a public `id()` or `getId()` method and a public `contents()` or `getContents()`
method may be used as a source, provided that Spring's `ConversionService` can
convert their return types to `UUID` and `String` respectively. If it has a
public `chapter()` or `getChapter()` method which can be converted to
`OptionalInt`, it sets the value of the `chapter` property; likewise, if it has
a public `newChapter()`, `getNewChapter()`, or `isNewChapter()` method that can
be converted to `boolean`, it overrides the `newChapter` property. Otherwise,
their default values are used.

If a clearance type has exactly one required property (and any number optional
properties), any object which can be converted to that property's type is also a
valid source object. Since `DocumentClearance`'s only required property is `id`,
anything that can be converted to `UUID` is a valid source object.

```java
record DocumentEditDto(String id, String contents) {}

DocumentEditClearance createEditClearance(DocumentEditDto dto) {
    return staticSecurityService
            .create(dto, DocumentEditClearance.class)
            .require();
}

Optional<DocumentEditClearance> requestEditClearance(DocumentEditDto dto) {
    return staticSecurityService
            .create(dto, DocumentEditClearance.class)
            .request();
}
```

Calling `require` or `request` checks all relevant access policies, and both
return the clearance if all policies permit it. If any policy denies access,
`require` throws the exception returned by that policy, while `request` returns
an empty result instead.

Ordinarily, access checks are tested for the user in Spring's security context.
This may be overridden by calling `forUser` before `request` or `require`.

In many cases, the source and clearance types are known in advance. In such a
case, a `ClearanceFactory` may be autowired, which specifies both types. If
the source type is not appropriate for the clearance type, the autowiring fails,
providing early feedback.

```java
@Autowired
private ClearanceFactory<DocumentEditDto, DocumentEditClearance> factory;

@Autowired(required = false)
private ClearanceFactory<String, DocumentEditClearance> invalid;

DocumentEditClearance createEditClearance(DocumentEditDto dto) {
    return factory.create(dto).require();
}

Optional<DocumentEditClearance> requestEditClearance(DocumentEditDto dto) {
    return factory.create(dto).request();
}
```

Spring's `ConversionService` may be used to convert source objects to clearance
types as well. If a Jackson `ObjectMapper` bean is available, it is given the
same capability.

The `ConversionService` and `ObjectMapper` integrations allow web applications
to receive clearances as endpoint method parameters. However, in many cases, a
clearance requires information from multiple parts of the request. For example,
a `PATCH` request conventionally accepts an ID on the path and contents in the
request body. This library provides a `MergedClearance` annotation to support
these cases.
