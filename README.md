# KLox

An interpreter for [Lox](http://craftinginterpreters.com). Written in
Kotlin.

### Notable differences to jLox

- C-style multiline comments (/* ... */) implemented; can be nested
- C-style ternary operator implemented
- Strings can be concatenated with anything
- Division by 0 causes a RuntimeError instead of NaN

### Build/Run

``` bash
# Run as REPL
./gradlew run

# Execute $file
./gradlew run $file
```

### Code style

Follow the style used by the default code formatter in IntelliJ IDEA.
Max line length is 150.