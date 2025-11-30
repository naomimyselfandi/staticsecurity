package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.Unwrap;
import io.github.naomimyselfandi.staticsecurity.web.MergedClearance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test/ing")
class TestController {

    record Helper(
            @PathVariable UUID id,
            @RequestParam String contents,
            @RequestParam(required = false) Integer chapter,
            @RequestParam(required = false, name = "new") Boolean createsNewChapter
    ) {}

    record HelperWithBody(@PathVariable UUID id, @RequestBody @Unwrap Map<String, Object> body) {}

    @PatchMapping("/foo/{id}")
    ResponseEntity<String> test(@MergedClearance(Helper.class) DocumentUpdateRequest request) {
        return ResponseEntity.ok(request.toString());
    }

    @PatchMapping("/bar/{id}")
    ResponseEntity<String> testWithUnwrappedBody(@MergedClearance(HelperWithBody.class) DocumentUpdateRequest request) {
        return test(request);
    }

}
