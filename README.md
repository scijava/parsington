[![](https://img.shields.io/maven-central/v/org.scijava/parsington.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.scijava%22%20AND%20a%3A%22parsington%22)
[![](https://github.com/scijava/parsington/actions/workflows/build-main.yml/badge.svg)](https://github.com/scijava/parsington/actions/workflows/build-main.yml)

# Parsington

Parsington is an infix-to-postfix and infix-to-syntax-tree expression parser
for mathematical expressions written in Java. It is simple yet fancy, handling
(customizable) operators, functions, variables and constants in a similar way
to what the Java language itself supports.

Parsington is part of the [SciJava](https://scijava.org/) project
for scientific computing in Java.

## Example

Infix input: `(-b + sqrt(b^2 - 4*a*c)) / (2*a)`  
&rarr; postfix output: `b - sqrt b 2 ^ 4 a * c * - (1) <Fn> + (1) 2 a * (1) /`

## Rationale

Expression parsers are as old as the hills; what makes this one different?

* __No dependencies.__
* __[Available on Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.scijava%22%20AND%20a%3A%22parsington%22).__
* __Permissive BSD-2 license.__ See [LICENSE.txt](LICENSE.txt).
* __Separation of concerns.__ Parsington's parser stands alone, giving you a
  postfix queue or syntax tree from your infix expression, which you can then
  process or evaluate however you see fit. Parsington also provides an [eval
  subpackage](src/main/java/org/scijava/parsington/eval) that can evaluate
  expressions involving objects of common types ({@code java.lang.Boolean},
  {@code java.lang.Number}, {@code java.lang.String}), and which is extensible
  to your own needs&mdash;there is no assumption that your variables will
  consist of any particular data type, numerical or otherwise.
* __Extensibility.__ The
  [default operators](src/main/java/org/scijava/parsington/Operators.java), a
  synthesis of Java and MATLAB syntax, work well. But if you need something
  else, you can define your own unary and binary operators with whatever
  symbols, precedence and associativity you desire.
* __Clean, well-commented codebase with unit tests.__ Import the source into
  your favorite IDE and watch Parsington in action by putting a breakpoint
  [here](src/main/java/org/scijava/parsington/ParseOperation.java#L70-L72).

## History

The [ImageJ Ops](https://github.com/imagej/imagej-ops) project needed an
expression parser so that it could be more awesome. But not one limited to
primitive doubles, or one that conflated parsing with evaluation, or one
licensed in a restrictive way. Just a simple infix parser: a nice shunting yard
implementation, or maybe some lovely recursive descent. Something on GitHub,
with no dependencies, available on Maven Central. But surprisingly, there was
only tumbleweed. So Parsington was born, and all our problems are now over!

## Usage

In your POM `<dependencies>`:
```xml
<dependency>
  <groupId>org.scijava</groupId>
  <artifactId>parsington</artifactId>
  <version>3.0.0</version>
</dependency>
```

### Postfix queues

To parse an infix expression to a postfix queue:
```java
LinkedList<Object> queue = new ExpressionParser().parsePostfix("a+b*c^f(1,2)'");
// queue = [a, b, c, f, 1, 2, (2), <Fn>, ^, ', *, +]
```

### Syntax trees

To parse an infix expression to a syntax tree:
```java
SyntaxTree tree = new ExpressionParser().parseTree("a+b*c^f(1,2)'");
```
```
       +-------+
       |   +   |
       +---+---+
           |
    +------+------+
    |             |
+---+---+     +---+---+
|   a   |     |   *   |
+-------+     +---+---+
                  |
           +------+------+
           |             |
       +---+---+     +---+---+
       |   b   |     |   '   |
       +-------+     +---+---+
                         |
                         |
                     +---+---+
                     |   ^   |
                     +---+---+
                         |
                  +------+------+
                  |             |
              +---+---+     +---+---+
              |   c   |     | <Fn>  |
              +-------+     +---+---+
                                |
                         +------+------+
                         |             |
                     +---+---+     +---+---+
                     |   f   |     |  (2)  |
                     +-------+     +---+---+
                                       |
                                +------+------+
                                |             |
                            +---+---+     +---+---+
                            |   1   |     |   2   |
                            +-------+     +-------+
```

### Evaluation

To evaluate an expression involving basic types:
```java
Object result = new DefaultTreeEvaluator().evaluate("6.5*7.8^2.3");
```

### Interactive console

There is also an [interactive console
shell](src/main/java/org/scijava/parsington/Main.java) you can play with.

Run it easily using [jgo](https://github.com/scijava/jgo):
```
jgo org.scijava:parsington
```

Or run from source, after cloning this repository:
```shell
mvn
java -jar target/parsington-*-SNAPSHOT.jar
```

Simple example invocations with the console's default evaluator:
```
> 6.5*7.8^2.3
732.3706691398969 : java.lang.Double
> 2*3,4
      ^
Misplaced separator or mismatched groups at index 4
```

The `postfix` built-in function lets you introspect a parsed postfix queue:
```
> postfix('6.5*7.8^2.3')
6.5 : java.lang.Double
7.8 : java.lang.Double
2.3 : java.lang.Double
^ : org.scijava.parsington.Operator
* : org.scijava.parsington.Operator
> postfix('[1, 2f, 3d, 4., 5L, 123456789987654321, 9987654321234567899]')
1 : java.lang.Integer
2.0 : java.lang.Float
3.0 : java.lang.Double
4.0 : java.lang.Double
5 : java.lang.Long
123456789987654321 : java.lang.Long
9987654321234567899 : java.math.BigInteger
[7] : org.scijava.parsington.Group
> postfix('f(x, y) = x*y')
f : org.scijava.parsington.Variable
x : org.scijava.parsington.Variable
y : org.scijava.parsington.Variable
(2) : org.scijava.parsington.Group
<Fn> : org.scijava.parsington.Function
x : org.scijava.parsington.Variable
y : org.scijava.parsington.Variable
* : org.scijava.parsington.Operator
= : org.scijava.parsington.Operator
> postfix('math.pow(q) = q^q')
math : org.scijava.parsington.Variable
pow : org.scijava.parsington.Variable
. : org.scijava.parsington.Operator
q : org.scijava.parsington.Variable
(1) : org.scijava.parsington.Group
<Fn> : org.scijava.parsington.Function
q : org.scijava.parsington.Variable
q : org.scijava.parsington.Variable
^ : org.scijava.parsington.Operator
= : org.scijava.parsington.Operator
```

The `tree` function is another way to introspect, in syntax tree form:
```
> tree('math.pow(q) = q^q')
 '='
 - '<Fn>'
  -- '.'
   --- 'math'
   --- 'pow'
  -- '(1)'
   --- 'q'
 - '^'
  -- 'q'
  -- 'q'
 : org.scijava.parsington.SyntaxTree
```

## Customization

Parsington supports various kinds of customization, including custom operators,
custom separator symbols, and even custom parsing of literals and/or other
expression elements. See the
[TestExamples](src/test/java/org/scijava/parsington/TestExamples.java) for
illustrations of these sorts of customizations in practice.
