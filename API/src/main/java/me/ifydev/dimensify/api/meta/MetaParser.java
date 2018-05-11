package me.ifydev.dimensify.api.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ifydev.dimensify.api.DimensifyConstants;

import java.util.Optional;

/**
 * @author Innectic
 * @since 5/11/2018
 */
public class MetaParser {

    @Getter
    @AllArgsConstructor
    public static class MetaResult {
        private Optional<String> error;
        private Optional<WorldMeta> meta;
    }

    public static MetaResult parse(String[] meta) {
        boolean structures = false;
        Optional<String> environment = Optional.empty();
        Optional<String> seed = Optional.empty();

        for (String part : meta) {
            if (part.equalsIgnoreCase("structure")) structures = true;
            else if (part.toLowerCase().startsWith("env=")) {
                String[] split = part.split("env=");
                if (split.length != 2)
                    return new MetaResult(Optional.of(DimensifyConstants.ENV_NOT_PROVIDED), Optional.empty());
                environment = Optional.of(split[1]);
            } else if (part.toLowerCase().startsWith("seed=")) {
                String[] split = part.split("seed=");
                if (split.length != 2)
                    return new MetaResult(Optional.of(DimensifyConstants.SEED_NOT_PROVIDED), Optional.empty());
                seed = Optional.of(split[1]);
            }
        }
        return new MetaResult(Optional.empty(), Optional.of(new WorldMeta(structures, environment, seed)));
    }
}
