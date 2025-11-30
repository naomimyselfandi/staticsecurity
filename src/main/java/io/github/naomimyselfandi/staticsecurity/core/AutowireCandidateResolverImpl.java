package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.ClearanceFactory;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;

record AutowireCandidateResolverImpl(
        @Delegate AutowireCandidateResolver delegate,
        BeanFactory beanFactory
) implements AutowireCandidateResolver {

    @Override
    public @Nullable Object getSuggestedValue(DependencyDescriptor descriptor) {
        var delegateResult = delegate.getSuggestedValue(descriptor);
        if (delegateResult != null) {
            return delegateResult;
        }
        var type = descriptor.getResolvableType();
        if (type.toClass() == ClearanceFactory.class) {
            var generics = type.getGenerics();
            var sourceType = generics[0].toClass();
            var targetType = generics[1].toClass().asSubclass(Clearance.class);
            var staticSecurityService = beanFactory.getBean(StaticSecurityService.class);
            // if the bean is required, always try to create it so there's a clear error message
            if (staticSecurityService.canCreate(sourceType, targetType) || isRequired(descriptor)) {
                return staticSecurityService.createFactory(sourceType, targetType);
            }
        }
        return null;
    }

    @Override
    public AutowireCandidateResolver cloneIfNecessary() {
        return new AutowireCandidateResolverImpl(delegate.cloneIfNecessary(), beanFactory);
    }

}
