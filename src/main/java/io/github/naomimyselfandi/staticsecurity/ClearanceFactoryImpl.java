package io.github.naomimyselfandi.staticsecurity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
class ClearanceFactoryImpl<S, C extends Clearance> implements ClearanceFactory<S, C> {

    final Class<C> type;
    final StaticSecurityService staticSecurityService;

    @Override
    public PendingClearance<C> create(S source) {
        return staticSecurityService.create(source, type);
    }

}
