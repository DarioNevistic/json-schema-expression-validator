package com.dnevi.expression.validator.schema;

public interface Visitor {

    void visit(Schema schema);

    void visitArraySchema(ArraySchema arraySchema);

    void visitBooleanSchema(BooleanSchema booleanSchema);

    void visitIntegerSchema(IntegerSchema integerSchema);

    void visitNumberSchema(NumberSchema numberSchema);

    void visitObjectSchema(ObjectSchema objectSchema);

    void visitStringSchema(StringSchema stringSchema);
}
