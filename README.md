# JSON Schema Expression Validator

### About
___
**Expression Validator** is an example application which demonstrates validation of condition expressions. It is a process which scans and validates expressions against JSON Schema in three phases.

 - First phase is a lexical analysis - scanning through the list of characters and group them together into the smallest sequences that represent something. We called them a lexemes.

 - In second phase we try to handle expression which can nest arbitrarily deeply. For that job to be done we must use [Context-Free grammar](https://en.wikipedia.org/wiki/Context-free_grammar) which has its own set of rules. In particular, we're defining an abstract syntax tree ([AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree)). In a parse tree, every single grammar production becomes a node in the tree. We parse a string - a series of tokens - then map those tokens to terminals in the grammar to figure out which rules could have generated that string.

    Used grammar:
    ```
    expression     → equality ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → primary ( ( ">" | ">=" | "<" | "<=" ) primary )* ;
    primary        → NUMBER | STRING | BOOLEAN | "nil" | "(" expression ")" ;
    ```

 - The third phase is evaluating expression - we recursively interpret each node from previous parsed tree and return boolean value if expression is valid or not.

___
### Notification pattern
Instead of throwing exceptions, validation errors are collected using [Notification Pattern](https://martinfowler.com/articles/replaceThrowWithNotification.html). 
Of course, if validation failed, you are able to fetch validation results using  `validator.getValidationResult()` method which is a part of the [expression validator](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/main/java/com/dnevi/expression/validator/expression/ExpressionValidator.java).
___
#### Interpreter
[Interpreter](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/main/java/com/dnevi/expression/validator/expression/Interpreter.java) evaluates expression using the **Visitor Pattern**. More details [here](https://en.wikipedia.org/wiki/Visitor_pattern).

#### Lexer
[Lexer](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/main/java/com/dnevi/expression/validator/expression/Lexer.java) performs the Lexical Analysis of expression.

#### Parser
[Parser](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/main/java/com/dnevi/expression/validator/expression/Parser.java) parses series of tokens - we map those tokens to terminals in the grammar to figure out could have generated that string.

___
### JSON Schemas
Supported JSON Schemas:
 - ObjectSchema 
 - IntegerSchema
 - NumberSchema
 - BooleanSchema
 - ArraySchema
 - StringSchema
___

### Example JSON Schema
```
{
  "definition": {
    "properties": {
      "name": {
        "validation": {
          "required": true
        },
        "type": "STRING"
      },
      "age": {
        "validation": {
          "required": true
        },
        "type": "INTEGER"
      },
      "employed": {
        "type": "BOOLEAN",
        "validation": {
          "required": true
        }
      }
    },
    "type": "OBJECT"
  }
}
```
Example JSON schema can be found [here](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/test/resources/example_state_schema.json).

### Example expressions:
```
($.age >= 30) || ($.age < 40)
$.name != null
$.name != \"null\"
$.name == \"someCoolName\"
$.employed == false
(($.employed == false) && ($.employed != true))
$.employed != false && $.age > 60
($.age != 30) || ($.age < 40) && ($.employed == true)
```
More details in [Expression Validator Test](https://github.com/DarioNevistic/json-schema-expression-validator/blob/master/src/test/java/com/dnevi/expression/validator/ValidatorApplicationTest.java).

___
### Installation
Run maven install command with
`mvn clean install`

Run maven tests
`mvn test`