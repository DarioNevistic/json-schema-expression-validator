package com.dnevi.expression.validator.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class StateSchema implements Serializable {

    private Schema definition;

    @JsonCreator
    public StateSchema(@JsonProperty("definition") Schema definition) {
        this.definition = definition;
        definition.markAsRoot();
        definition.setId("root");
    }

    /**
     * @return Flattened list of schema ids
     */
    @JsonIgnore
    public List<String> getFlattenedSchemaIds() {
        List<String> ids = new ArrayList<>();
        return this.extractSchemaIds(ids, this.getDefinition());
    }


    // @formatter:off
    /**
     * If the client provides destination schema id like "root/A/B/C/D", method will return the following list
     * <p>
     * "root"
     * "root/A"
     * "root/A/B"
     * "root/A/B/C"
     * "root/A/B/C/D"
     * </p>
     *
     * @param destinationId Destination schema id
     * @return List of all schemas from root schema to destination schema
     */
    // @formatter:on
    @JsonIgnore
    static List<String> findAllIdsFromRootSchemaToSchemaWithId(String destinationId) {
        List<String> nodes = new ArrayList<>();

        String[] t = destinationId.split("/");

        int i;
        for (i = 0; i < t.length; i++) {
            if (i == 0) {
                nodes.add(t[i]);
            } else {
                String previous = nodes.get(i - 1);
                nodes.add(previous.concat("/").concat(t[i]));
            }
        }

        return nodes;
    }

    /**
     * Method will check if all nodes except the last one are instance of {@link ObjectSchema} and
     * the last one can be instance of {@link Schema}
     *
     * @param path {@link StatePath}
     * @return Return boolean value
     */
    @JsonIgnore
    public boolean isValidStatePath(StatePath path) {
        String destinationId = path.transformToSchemaId();
        List<String> nodes = StateSchema.findAllIdsFromRootSchemaToSchemaWithId(destinationId);

        int size = nodes.size();

        String lastNode = nodes.stream().skip(size - 1L).findFirst()
                .orElseThrow(RuntimeException::new);
        List<String> objectNodes = nodes.stream().limit(size - 1L).collect(Collectors.toList());

        Boolean allMatch = objectNodes.stream().allMatch(this::isSchemaIdTypeObject);
        Boolean hasSchema = this.hasSchemaWith(lastNode);

        return allMatch && hasSchema;
    }

    /**
     * Method will try to find schema by schema id
     *
     * @param id Schema id
     * @param schema Searchable schema {@link Schema}
     * @return Optional schema
     */
    @JsonIgnore
    public static Optional<Schema> findSchemaById(String id, Schema schema) {
        if (schema.getId().equals(id)) {
            return Optional.of(schema);
        }

        if (schema instanceof ObjectSchema) {
            Map<String, Schema> schemaMap = ((ObjectSchema) schema).getProperties();
            for (Map.Entry<String, Schema> entry : schemaMap.entrySet()) {
                Optional<Schema> optionalSchema = StateSchema.findSchemaById(id, entry.getValue());
                if (optionalSchema.isPresent()) {
                    return optionalSchema;
                }
            }
        }

        if (schema instanceof ArraySchema) {
            Schema arraySchemaItems = ((ArraySchema) schema).getItems();
            Optional<Schema> optionalSchema = StateSchema.findSchemaById(id, arraySchemaItems);

            if (optionalSchema.isPresent()) {
                return optionalSchema;
            }
        }

        return Optional.empty();
    }

    @JsonIgnore
    public Optional<Schema> findSchemaByStatePath(StatePath statePath) {
        boolean isValid = this.isValidStatePath(statePath);

        if (isValid) {
            return StateSchema
                    .findSchemaById(statePath.transformToSchemaId(), this.getDefinition());
        }

        return Optional.empty();
    }

    @JsonIgnore
    private boolean isSchemaIdTypeObject(String schemaId) {
        Optional<Schema> optionalSchema = StateSchema
                .findSchemaById(schemaId, this.getDefinition());

        return optionalSchema.isPresent() && optionalSchema.get() instanceof ObjectSchema;
    }

    @JsonIgnore
    private boolean hasSchemaWith(String schemaId) {
        Optional<Schema> optionalSchema = StateSchema
                .findSchemaById(schemaId, this.getDefinition());

        return optionalSchema.isPresent();
    }

    // @formatter:off
    /**
     * Method will recursively go through all child schemas and extract ids Example of returned
     * list:
     *
     * <p>
     * "root/A"
     * "root/A/B"
     * "root/A/BB"
     * "root/B"
     * "root/C"
     * "root/C/A
     * </p>
     *
     * @param ids Flattened List of schema ids
     * @param schema Schema used for id extraction
     * @return Flattened list of ids in the Schema
     */
    // @formatter:on
    @JsonIgnore
    private List<String> extractSchemaIds(List<String> ids, Schema schema) {
        ids.add(schema.getId());

        if (schema instanceof NestableSchema) {
            ((NestableSchema) schema).getChildSchemas()
                    .forEach((k, v) -> this.extractSchemaIds(ids, v));
        }

        return ids;
    }
}
