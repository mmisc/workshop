# Jazzer Fuzzing Workshop

Hands-on exercises for learning coverage-guided fuzzing on the JVM with
[Jazzer](https://github.com/CodeIntelligenceTesting/jazzer), designed for
a 90-minute practical session.

Accompanies the worksheet in `WORKSHEET.md`. Keep both open.

## What you will learn

1. How to read Jazzer's libFuzzer-style output (`cov`, `ft`, `corp`, `exec/s`, `rss`).
2. How to write a `@FuzzTest` from scratch, with `byte[]` and with `FuzzedDataProvider`.
3. What makes a good vs. bad fuzz target, and how to recognize and fix fuzz blockers.
4. How dictionaries help on keyword-gated parsers.
5. How Jazzer's instrumentation scope trades coverage against throughput.
6. How Jazzer's bug detectors (sanitizers) find security bugs that ordinary exception-based tests miss.

## Prerequisites

* JDK 17 (exactly 17 is tested; 21 should also work). Check with `java -version`.
* Maven 3.8+.
* An IDE with JUnit 5 support (IntelliJ IDEA, VS Code, Eclipse).
* A network connection for the first build. Maven will download roughly 200 MB of dependencies because the SBB Spring Cloud Stream Binder (used by Exercise 06) pulls in Spring Boot 4 and the Solace JCSMP client transitively. **Please run the setup step below on a good connection BEFORE the workshop.**

## Setup

Clone this repo, then from the project root run:

```
mvn test-compile
```

This downloads all dependencies and compiles both the production and the test sources. You do not need a Solace broker or any other external service.

If the compile step passes, you are ready.

## How Jazzer's two modes work

Every `@FuzzTest` method can run in one of two modes:

### Regression mode (default, what `mvn test` gives you)

Jazzer runs the test once per input file in the corresponding `src/test/resources/.../<ClassName>Inputs/` directory. No mutation, no coverage-guided exploration. This is how you verify that previously found bugs stay fixed, and it is what CI should run.

```
mvn test
```

### Fuzzing mode (what you want during the workshop)

Jazzer takes over the test method, mutates inputs to maximize coverage, and reports findings. Enabled with the `JAZZER_FUZZ=1` environment variable:

```
JAZZER_FUZZ=1 mvn -Dtest=Exercise01Test test
```

Note: in fuzzing mode only one `@FuzzTest` method runs per Maven invocation, so always use `-Dtest=<ClassName>` to select which exercise you are working on.

On Windows PowerShell:

```
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise01Test test
```

On Windows cmd.exe:

```
set JAZZER_FUZZ=1 && mvn -Dtest=Exercise01Test test
```

## Where things live

```
pom.xml                                      Build config, dependencies
WORKSHEET.md                                 Fill-in-the-blanks worksheet
src/main/java/de/unibonn/fuzzing/
  exercise01/AliasResolver.java              Works with Exercise01Test
  exercise02/EnvParser.java                  Works with Exercise02Test
  exercise03/RequestProcessor.java           Works with Exercise03Test
  exercise04/PacketParser.java               Works with Exercise04Test
  exercise05/QueryEngine.java                Works with Exercise05Test
  exercise06/QueueNameResolver.java          Works with Exercise06Test
  exercise07/TemplateReader.java             Works with Exercise07Test
src/test/java/de/unibonn/fuzzing/
  Exercise01Test.java ... Exercise07Test.java
src/test/resources/de/unibonn/fuzzing/
  Exercise01TestInputs/                      Seed input for regression mode
  exercise05.dict                            libFuzzer-format dictionary
```

## Exercises at a glance

| # | Topic                                      | Class                |
|---|--------------------------------------------|----------------------|
| 1 | Read Jazzer output on a ready-to-run test  | `Exercise01Test`     |
| 2 | Write your first fuzz target from scratch  | `Exercise02Test`     |
| 3 | Refactor a deliberately bad fuzz target    | `Exercise03Test`     |
| 4 | Get past a CRC checksum (a fuzz blocker)   | `Exercise04Test`     |
| 5 | Use a dictionary on a keyword-gated parser | `Exercise05Test`     |
| 6 | Control instrumentation scope              | `Exercise06Test`     |
| 7 | Trigger the path traversal sanitizer       | `Exercise07Test`     |

Each exercise file has detailed in-line instructions at the top. Work through them in order; the worksheet will prompt you for observations at each step.

## Where crash files go

When Jazzer finds a crashing input, it writes two files:

* `crash-<sha1>` in the project's working directory, containing the raw bytes.
* `Crash_<sha1>.java` next to it, containing a standalone reproducer.

Move the raw bytes file into `src/test/resources/de/unibonn/fuzzing/<TestClass>Inputs/` to make it a permanent regression case that runs in every `mvn test`.

## Things you will see that look like errors but aren't

* `libFuzzer: deadly signal` in the output is the FINDING, not a tool failure. Jazzer found the bug you wanted.
* `WARNING: A Java agent has been loaded dynamically` is silenced by `-XX:+EnableDynamicAgentLoading` in the `pom.xml`. If you see it from an IDE run that bypasses the pom, it is harmless.
* `INFO: Instrumented <class>` lines during startup mean Jazzer's agent did its job. Expect hundreds of these for Exercise 06 at default scope.
* Very low `exec/s` on the first iteration is normal; the JVM needs to warm up and JIT-compile the hot paths. Wait 5 to 10 seconds before judging throughput.

## If something goes wrong

* **"No tests were executed"**: in fuzzing mode you must pick exactly one test class with `-Dtest=<ClassName>`. Also check that your `@FuzzTest` method is non-private and has at least one parameter.
* **ARM Mac**: Jazzer ships native components. Version 0.30.0 has aarch64 support for macOS, but if you hit a linker error, please tell us.
* **"jazzer.jar not found" / unsatisfied link error**: re-run `mvn test-compile` to make sure dependencies are downloaded.
* **First build is extremely slow**: that is the 200 MB of transitive Spring / Solace dependencies. This is a one-time cost.

## References

* Jazzer README: https://github.com/CodeIntelligenceTesting/jazzer
* Advanced flags: https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/advanced.md
* libFuzzer output format: https://llvm.org/docs/LibFuzzer.html#output
