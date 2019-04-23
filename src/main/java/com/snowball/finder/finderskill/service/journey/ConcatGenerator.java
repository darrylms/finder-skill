package com.snowball.finder.finderskill.service.journey;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Builder
public class ConcatGenerator {
    @Builder.Default
    Optional<String> current = Optional.empty();
    @NonNull Random random;
    @NonNull List<String> options;
    public String next(){
        if (current.isPresent()){
            List<String> ops = options.stream()
                    .filter(v -> !current.get().equals(v))
                    .collect(Collectors.toList());
            current = Optional.of(ops.get(random.nextInt(ops.size())));
        } else {
            current = Optional.of("");
        }
        return current.get();
    }
}
