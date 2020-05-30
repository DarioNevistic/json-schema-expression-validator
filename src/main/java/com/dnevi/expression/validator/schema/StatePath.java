package com.dnevi.expression.validator.schema;

import com.dnevi.expression.validator.exception.InvalidStatePathStringException;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class StatePath {

    private String path;

    @JsonCreator
    public StatePath(String path) {
        this.assertPath(path);
        this.path = path;
    }

    /**
     * Method will make transformation from path to schema id string
     *
     * Path example: $.a.b.c.d
     *
     * Schema Id: root/a/b/c/d
     *
     * @return Transformed path to schema id
     */
    public String transformToSchemaId() {
        return this.getPath().replaceFirst("^\\$.", "root/").replace(".", "/");
    }

    private void assertPath(String path) {
        Objects.requireNonNull(path, "Path cannot be null");
        Matcher matcher = Pattern.compile("^\\$.").matcher(path);

        if (!matcher.find()) {
            throw new InvalidStatePathStringException(path);
        }
    }


}
