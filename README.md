## Curta

A small, customizable expression evaluator. Named after the [mechanical calculator
introduced by Curt Herzstark](http://en.wikipedia.org/wiki/Curta) in 1948. 

The parser is generated using [JavaCC](http://javacc.java.net). The generated parser 
classes are *not* a part of this repository, only [the grammar](https://github.com/bkiers/Curta/blob/master/src/grammar/CurtaParser.jjt)
is included. You can generate them using `ant generate`.

It supports all static methods, and variables, from 
[Java 7's Math class](http://docs.oracle.com/javase/7/docs/api/java/lang/Math.html) 
and can be easily extended with extra functions. Even certain expressions can be 
programatically changed. For example, changing the meaning of the `^` as a binary 
XOR operator to act as a power operator (see [Changing expressions](#changing-expressions)
below).

## Installation

Either checkout this repository and run: `ant jar`, or download the 
[JAR file](https://github.com/bkiers/Curta/blob/master/Curta-0.3.jar) directly 
and add it to your project's classpath.

## Quick demo

For a more thorough explanation of this library, scroll passed [the next paragraph](#data-types), 
but for the impatient, here's a quick demo of how to use *Curta*:

```java
// soon
```

## Data types

The following datatypes are supported:

* number (`double` and `long`)
* boolean (`true` and `false`)
* null (`null`)

## Operators

Below follows a list of all supported operators, starting with the ones with
the highest precedence.

1. grouped expressions: `(` ... `)`
2. power: `**`
3. bitwise not: `~`
4. <i></i>
   * unary minus: `-`
   * unary plus: `+`
   * not: `!`
5. <i></i>
   * multiply: `*`
   * division: `/`
   * modulus: `%`
6. <i></i>
   * add: `+`
   * subtract: `-`
7. <i></i>
   * signed left shift: `<<`
   * signed right shift: `>>`
   * unsigned right shift: `>>>`
8. <i></i>
   * less than: `<`
   * less than or equal: `<=`
   * greater than: `>`
   * greater than or equal: `>=`
9. <i></i>
   * equal: `==`
   * not equal: `!=`
10. bitwise AND: `&`
11. bitwise XOR: `^`
12. bitwise OR: `|`
13. AND: `&&`
14. OR: `||`

## Variables

The variables `PI` and `E` from [Java 7's Math class](http://docs.oracle.com/javase/7/docs/api/java/lang/Math.html) 
are built-in, and you can assign variables yourself too, either programatically, or in the input that
is to be evaluated. Some examples:

```java
Curta curta = new Curta();

System.out.println(curta.eval("PI"));
System.out.println(curta.eval("E"));

curta.addVariable("answer", 21);

System.out.println(curta.eval("answer * 2"));

System.out.println(curta.eval("x = 5; answer * x"));
```

which will print:

```
3.141592653589793
2.718281828459045
42.0
105.0
```

As you can see in the previous example, you can evaluate more than one statement/expression 
at the same time. The last expression is returned by `Curta`'s `eval(...)` method. You can
either evaluate `String`s, or `File`s. If you evaluate more than one statement/expression,
you need to separate them with a semi colon, `';'`, or a line break.

## Functions

#### Built-in functions

All methods from [Java 7's Math class](http://docs.oracle.com/javase/7/docs/api/java/lang/Math.html) 
are supported. However, there's no need to put `Math.` in front of it. 

Some examples:

```java
Curta curta = new Curta();

System.out.println(curta.eval("sin(1)"));
System.out.println(curta.eval("random()"));
System.out.println(curta.eval("hypot(3, 4)"));
System.out.println(curta.eval("abs(-1)"));
System.out.println(curta.eval("min(42, -11)"));
```

would print:

```
0.8414709848078965
0.6805327383776745
5.0
1.0
-11.0
```

*(of course, the call to `random()` is most likely to result in something different... :))*

#### Custom functions

You can also define your own functions programatically. For example, you would like
to create a function that will return `true` (or `false`) if a number is prime or not.

You can use `Curta`s `addFcuntion(Function)` method for this:

```java
Curta curta = new Curta();

curta.addFunction(new Function("isPrime") {

    @Override
    public Object eval(Object... params) {

        // number of parameters must be exactly 1 (min=1, max=1)
        super.checkNumberOfParams(1, 1, params);

        // expecting an integer value (not floating-point!)
        long value = super.getLong(0, params);

        // return whether this number is prime
        return new java.math.BigInteger(String.valueOf(value)).isProbablePrime(20);
    }
});

String expression = "isPrime(2147483647)";

System.out.printf("%s = %s\n", expression, curta.eval(expression)); 
```

which will print: `isPrime(2147483647) = true`

As you can see, the abstract class [`Function`](https://github.com/bkiers/Curta/blob/master/src/main/curta/function/Function.java), 
which your custom function must be extended from, has a couple of utility methods 
to check the number of parameters, and convert these parameters to more usable 
classes (a `long`, in this case). This means that evaluating either `"isPrime(true)"`
or `"isPrime(11, 17)"` would result in an exception to be thrown.

## Changing expressions

Some expressions can be changed (or removed) from the parser programatically. Lets's say
you don't want to have support for the *bitwise-not* operator. You can then simply set the 
operator of an expression to `null`.

For example, the following code:

```java
Curta curta = new Curta();
System.out.println(curta.eval("~42"));
```

would print `-43`, but:

```java
Curta curta = new Curta();
curta.setExpression(Operator.BitNot, null);
System.out.println(curta.eval("~42"));
```
would throw the following exception: 

`Exception in thread "main" java.lang.RuntimeException: not implemented: ~ (BitNot)`

One more example. Let's say you want to support `final` variables. Whenever a variable 
consists of only capital letters (or underscores), you don't want that variable to ever 
change. This is how you could go about doping this:

```java
Curta curta = new Curta();

curta.setExpression(Operator.Assign, new Expression() {
    @Override
    public Object eval(CurtaNode ast, Map<String, Object> vars, Map<String, Function> functions, Map<Integer, Expression> expressions) {

        // get the text of the 1st child
        String id = super.getTextFromChild(0, ast);

        // evaluate the 2nd child
        Object value = super.evalChild(1, ast, vars, functions, expressions);

        // check if it's made from caps only, and is already defined
        if(id.matches("[A-Z_]+") && vars.containsKey(id)) {
            System.err.println("cannot reassign: " + id);
        }
        else {
            vars.put(id, value);
        }

        return value;
    }
});

System.out.println(curta.eval("PI = 3; VAR = 42; VAR = -1; VAR"));
```

The code above will return `42` and causes the following to be printed to the `system.err`:

```
cannot reassign: PI
cannot reassign: VAR
```

Changing `VAR` to `var` will cause `-1` to be returned.

Calling `clear()` on the `Curta` instance will reset everything: the *bitwise-not* is supported 
again, and you can reassign capitalized variables.
