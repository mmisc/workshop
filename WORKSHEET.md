# Jazzer Fuzzing Workshop - Worksheet

Keep this file open next to your IDE and fill in the notes as you go.
There are no right or wrong answers in the note boxes; the goal is to
build your own reading of what Jazzer is telling you. We will discuss
your answers between exercises.

**How to use this worksheet**

- Work through the checkpoints in order.
- Where you see `> _Your notes:_`, write what you observe in your own words.
- Where you see `> _Prediction:_`, write your guess BEFORE running the command.
- Where you see a checklist `[ ]`, tick it off when done.

All commands assume you are in the project root and have run `mvn test-compile` at least once.
Each command block shows a Linux/macOS variant and a Windows (PowerShell) variant.

### Resetting an exercise

Each run accumulates a corpus and crash inputs. To start an exercise from scratch,
replace `Exercise05` with the exercise you want to reset:

```bash
# Linux / macOS — reset one exercise
ex=Exercise05
rm -rf ".cifuzz-corpus/de.unibonn.fuzzing.${ex}Test" \
       "src/test/resources/de/unibonn/fuzzing/${ex}TestInputs"
```
```powershell
# Windows (PowerShell) — reset one exercise
$ex = "Exercise05"
Remove-Item -Recurse -Force ".cifuzz-corpus\de.unibonn.fuzzing.${ex}Test" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "src\test\resources\de\unibonn\fuzzing\${ex}TestInputs" -ErrorAction SilentlyContinue
```

To reset everything at once:

```bash
# Linux / macOS — reset all exercises
rm -rf .cifuzz-corpus src/test/resources/de/unibonn/fuzzing/*TestInputs
```
```powershell
# Windows (PowerShell) — reset all exercises
Remove-Item -Recurse -Force ".cifuzz-corpus" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "src\test\resources\de\unibonn\fuzzing\*TestInputs" -ErrorAction SilentlyContinue
```

The `target/` directory mirrors `src/test/resources` and is repopulated automatically
by the next Maven run — no need to clean it separately.

---

## Checkpoint 0 - Setup check

Run the ready-made fuzz test from the command line:

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise01Test test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise01Test test
```

It should find a crash within about a minute. This is expected — watch the status lines while it runs.

- [ ] Jazzer produced periodic status lines in the terminal.
- [ ] A file starting with `crash-` appeared.

> _Your notes: where exactly did the crash file appear?_
>
>
>

---

## Checkpoint 1 - Reading the Jazzer status line

Look at the periodic lines Jazzer prints while it runs. They look like:

```
#1234  NEW    cov: 42 ft: 58 corp: 7/128b lim: 80 exec/s: 1200 rss: 180Mb
```

Write down, in your own words, what each of these means:

| Field     | What does it mean? |
|-----------|--------------------|
| `#1234`   |                    |
| `NEW`     |                    |
| `cov`     |                    |
| `ft`      |                    |
| `corp`    |                    |
| `exec/s`  |                    |
| `rss`     |                    |

> _Which of these numbers would you look at first if you suspected your fuzz target was too slow? Why?_
>
>
>

> _Which would you look at first if you suspected your fuzz target was not reaching interesting code?_
>
>
>

---

## Checkpoint 2 - Findings vs. ordinary exceptions

Open the `crash-<sha1>` file from Checkpoint 0 and look at the top of the stack trace Jazzer printed to the terminal.

> _What exception type did Jazzer report?_
>
>

Jazzer distinguishes between two kinds of findings:

1. **Security issues** (`FuzzerSecurityIssueLow/Medium/High/Critical`): raised by Jazzer's bug detectors for specific vulnerability patterns.
2. **Uncaught exceptions**: anything your target throws that is not caught, treated as a crash by default.

> _Which of the two did you just see? How can you tell?_
>
>

---

## Checkpoint 3 - Writing your first fuzz target

Open `src/test/java/de/unibonn/fuzzing/Exercise02Test.java`. The class is almost empty. Your job is to write a `@FuzzTest` that calls `EnvParser.parse(String)`.

Start with the simplest possible signature: a single `FuzzedDataProvider` parameter, and call `data.consumeRemainingAsString()` inside. You can copy the imports from Exercise 01 as a starting point.

- [ ] My target compiles.
- [ ] My target actually calls `EnvParser.parse`.
- [ ] Running the command below produces Jazzer output (not "no tests found").

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise02Test test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise02Test test
```

> _Did Jazzer find anything? If yes, what? If no, note the last `cov` and `exec/s` values before you stopped it._
>
>
>

**Note — typed consume methods for more complex targets:**
`FuzzedDataProvider` offers more than just `consumeRemainingAsString()`. When the code under test takes several parameters — or when you want to construct a structured object from fuzz bytes — you can carve the byte stream into typed pieces:

```java
int    port    = data.consumeInt(1, 65535);       // bounded int
boolean useTls = data.consumeBoolean();
String  host   = data.consumeRemainingAsString();  // rest of the stream
```

Each call consumes bytes from the front of the shared stream in order, so the ORDER of calls determines how mutations map to each parameter. Use `consumeRemainingAsString()` (or `consumeRemainingAsBytes()`) last — once called, no bytes are left for subsequent calls. For targets that parse a single string (like `EnvParser`), the plain `consumeRemainingAsString()` approach you already have is the right choice.

---

## Checkpoint 4 - Fuzz blockers

You just fixed some problems in your OWN code. There is a related concept for problems in the CODE UNDER TEST: a **fuzz blocker** is a piece of code in the target that prevents the fuzzer from reaching interesting inputs no matter how long it runs. Common examples:

- A checksum or magic-byte check at the top of a parser.
- A size or length check that rejects most inputs.
- A call to `System.currentTimeMillis` or `Math.random` that makes behavior non-deterministic.
- Heavy early validation that returns before reaching the code you care about.

Open `Exercise04Test.java`. The naive fuzz target passes raw fuzzer bytes to `PacketParser.parse`. Run it for 60 seconds:

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise04Test test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise04Test test
```

> _Where does `cov` plateau?_
>
>

Now read `PacketParser.java` and identify the fuzz blocker.

> _What did you find? Is it a "real" check (something a real attacker would also have to satisfy) or an "artificial" one (something you can safely bypass while fuzzing)?_
>
>

The file contains a commented hint showing one way to get past the blocker by computing a valid checksum inside the fuzz target. Apply the fix (either adapt the hint or write your own), then re-run.

> _What does `cov` look like now? Did Jazzer find any new bugs below the CRC gate?_
>
>

---

## Checkpoint 6 - Dictionaries

Open `Exercise05Test.java`. The target parses a SQL-like language (`SELECT`, `INSERT INTO`, `CREATE TABLE`, ...). Without help, Jazzer has to guess these keywords byte-by-byte.

First, run without a dictionary for 30 seconds. We also pass `-Djazzer.trace=cov` to
disable Jazzer's comparison tracing (`cmp`), which would otherwise silently act as an
auto-dictionary and hide the effect we want to observe:

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise05Test -Djazzer.trace=cov test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise05Test "-Djazzer.trace=cov" test
```

> _What is `cov` after 30 seconds?_
>
>

Now uncomment the `@DictionaryFile(resourcePath = "exercise05.dict")` line in the test file.

> _Prediction: how much will `cov` change?_
>
>

Run it again (keep `-Djazzer.trace=cov` so only the dictionary changes between the two runs):

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise05Test -Djazzer.trace=cov test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise05Test "-Djazzer.trace=cov" test
```

> _Actual result? Under what conditions do you think dictionaries help the most?_
>
>

---

## Checkpoint 7 - Instrumentation and its effects

In JUnit mode, Jazzer instruments only your project's own compiled classes by
default — third-party library JARs on the classpath are **not** instrumented
unless you explicitly add them. That sounds conservative, but widening the scope
has a cost in throughput, and the extra coverage signal is not always useful.

Open `Exercise06Test.java`. `QueueNameResolver.resolve` delegates almost all of
its work to `SolaceProvisioningUtil.getQueueNames()`, which internally evaluates
two SpEL expressions and validates queue names against Solace naming rules.

First run: default scope (only your code instrumented).

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise06Test test
```

Record after 30 seconds:

> `cov` = ________   `exec/s` = ________

Now widen the scope to also instrument the Solace binder. Jazzer uses the
platform path separator to split the list (`:` on Linux/macOS, `;` on Windows):

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test \
    -Djazzer.instrumentation_includes='de.unibonn.fuzzing.**:com.solace.**' \
    test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise06Test `
    "-Djazzer.instrumentation_includes=de.unibonn.fuzzing.**;com.solace.**" `
    test
```

Record after 30 seconds:

> `cov` = ________   `exec/s` = ________

> _Which went up? Which went down? Explain the trade-off in your own words._
>
>

> _Under what circumstances would you accept the slower `exec/s` in exchange for higher `cov`? When would you want the reverse?_
>
>

Bonus: widen further to also instrument Spring Expression (the SpEL engine that
Solace uses internally):

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test \
    -Djazzer.instrumentation_includes='de.unibonn.fuzzing.**:com.solace.**:org.springframework.expression.**' \
    test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise06Test `
    "-Djazzer.instrumentation_includes=de.unibonn.fuzzing.**;com.solace.**;org.springframework.expression.**" `
    test
```

> _Where does this land compared to the previous two runs?_
>
>

---

## Checkpoint 8 - A bug detector in action

So far all your findings have been uncaught exceptions: NPE, AIOOBE, ArithmeticException. Jazzer also has built-in detectors for specific security-relevant patterns: SQL injection, path traversal, SSRF, insecure deserialization, and more. These encode knowledge about vulnerability classes.

Open `Exercise07Test.java`. `TemplateReader.readTemplate` reads a file whose path is partly under the caller's control.

> _Before running: can you spot the bug by reading the code?_
>
>

Run the test:

```bash
# Linux / macOS
JAZZER_FUZZ=1 mvn -Dtest=Exercise07Test test
```
```powershell
# Windows (PowerShell)
$env:JAZZER_FUZZ=1; mvn -Dtest=Exercise07Test test
```

Jazzer should report a `FuzzerSecurityIssue*` rather than an ordinary exception.

> _What exactly did it report? How does the output differ from the plain exception findings you saw earlier (e.g., Exercise 01)?_
>
>

Now apply the fix commented at the bottom of `TemplateReader.java`, then re-run.

> _Does Jazzer still find anything? Why not?_
>
>

> _In your own words, why is this kind of built-in detector more useful than just waiting for an exception?_
>
>

---

## Final reflection

Take 3 minutes to answer these, in your own words, for the group discussion:

1. If you had to explain to a colleague in one sentence what `cov` going up means, what would you say?

2. What is the first thing you would check if someone told you "my Jazzer run found nothing after 10 minutes"?

3. Name one thing you would change about how you write unit tests after today.

4. What is one thing you still feel unclear about?

---

_End of worksheet. Thanks for participating._
