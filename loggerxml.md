# Logger XML Specification

Logger generator produces `TsetlinLogger.h` and `TsetlinLoggerDefs.h`. The reasoning is that `TsetlinLoggerDefs.h` contains definitions of logger-specific data structures that can be used by a TM implementation. And the logger itself is contained in `TsetlinLogger.h`, which is likely to reference the TM implementation. Therefore, to avoid cyclic referencing, the logger API is split between these two files.

In other words:
* In TM implementation use: `#include "TsetlinLoggerDefs.h"`.
* In `TsetlinLogger.h` include TM headers via `<include>` element in `logger.xml`.

Example XML file: [logger.xml](logger.xml)

## &lt;logger&gt;

_Root element._

_No attributes._

Can contain sections `<defs>`, `<spectrum>`, `<status>`, and also `<include>` elements.

## &lt;defs&gt;

_No attributes._

Specifies includes for `TsetlinLoggerDefs.h`. Can contain only `<include>` elements in the current version of API.

## &lt;include&gt;

_Attributes:_

| attribute | description |
| :--- | :--- |
| **file** | include file name. |

Creates an include directive in the `TsetlinLogger.h` file if placed under `<logger>` element, or in the `TsetlinLoggerDefs.h` file if placed in the `<defs>` section.

## &lt;spectrum&gt;

This section creates TA spectrum log.

TA spectrum log is a CSV file where each column corresponds to a TA state and the rows contain integer numbers of TAs in this state at each point in time.

This section generates the `LogTAStates` helper structure in `TsetlinLogger.h` and the following functions:
* `void startLogTAStates(int cls, LogTAStates* log)` &ndash; initialize TA spectrum log file for class `cls`.
* `void logTAStates(LogTAStates* log, int step, [additional parameters])` &ndash; add next row of data into the log using the current state of the TM; `step` can be used to identify the time step within the log, it is logged in the beginning of each data row.
* `void finishLogTAStates(LogTAStates* log)` &ndash; close the log file.

Note that `LogTAStates` structure refers to a single-class TA spectrum.

_Attributes:_

| attribute | description |
| :--- | :--- |
| **path** | output file name format where `%d` will be replaced with the class id |
| **enable** | the name of the global variable to toggle this output; the variable will be automatically added to `TsetlinLoggerDefs.h` |
| **minstate** | C expression that gives the index of the lowest (extreme exclude) state of the TAs |
| **maxstate** | C expression that gives the index of the highest (extreme include) state of the TAs |
| **clauses** | C expression (or constant) that gives the number of clauses per class |
| **literals** | C expression (or constant) that gives the number of literals in a clause |

Requires `<tastates>` subsection; `<params>` subsection is optional.

### &lt;params&gt;

_No attributes._

_Inner text or CDATA:_ additional parameters to the `logTAStates` function. Normally, this should at least provide a pointer to the TM object and maybe the class index.


### &lt;tastate&gt;

_No attributes._

_Inner text or CDATA:_ C expression that gives the TA state for the literal `k` in the clause `j` within a single-class TM.


## &lt;status&gt;

This section creates a TM status log that can write arbitrary events and variables. You can have multiple `<status>` sections in one logger, each will create separate log file (or set of files).

* **Event** probes can be placed anywhere in the TM code; the tool counts the number of times an event is encountered during each measurement window. Example events: type I and II feedbacks, decision flips, etc.
* **Variables** are properties and metrics that can be calculated as a function of the TM state. The tool recalculates and logs variables at the end of each measurement window. Example variables: inference accuracy, number of included literals, etc.

The **name** attribute is used to name the generated types and functions for this log section. In the following list, `NAME` will be replaced with this attribute's value.

This section generates the `LogName` helper structure in `TsetlinLogger.h` and the following functions:
* `void startLogNAME([int cls], LogName* log)` &ndash; initialize log file; `cls` is used for class-scope logs.
* `void logNAME(LogName* log, int step, [additional parameters])` &ndash; write next log entry.
* `void finishLogNAME(LogName* log)` &ndash; close the log file.

It also enerates helper macros in `TsetlinLoggerDefs.h`:
* `TM_COUNTERS` &ndash; the list of event counter variables; put this macro in the body of a single-class TM structure or class.
* `RESET_COUNTERS(tm)` &ndash; put all counters to their specific reset values.
* `COUNT(c)` &ndash; increment counter `c`.

_Attributes:_

| attribute | description |
| :--- | :--- |
| **name** | name of the log section used to generate C function and structure names |
| **scope** | `class` or `global`; indicates whether log files should be created per class or just one log file for all classes |
| **path** | output file name format; for per-class log files, `%d` will be replaced with the class id |
| **enable** | the name of the global variable to toggle this output; the variable will be automatically added to `TsetlinLoggerDefs.h` |

### &lt;params&gt;

_No attributes._

_Inner text or CDATA:_ additional parameters to the `logNAME` function. Normally, this should at least provide a pointer to the TM object and maybe the class index.


### &lt;access&gt;

_No attributes._

_Inner text or CDATA:_ C code for accessing counters within from a TM object.

> Not implemented yet! The access is currently hard-coded as `tm->`.

Used to generate counter-related functions and macros:

* In `RESET_COUNTERS` macro: `tm->counter = reset`.
* In `logNAME` function: the value is read as `tm->counter`.

### &lt;var&gt;

_Attributes:_

| attribute | description |
| :--- | :--- |
| **id** | variable name, also used as a column header in the CSV |
| **type** | (Optional, default is `float`) type of data: `int`, `long`, `float`, or `double` |
| **cache** | (Optional) name of the struct member to cache the value of the variable; will be added to `LogName` structure |
| **group** | (Optional) if specified, the variable represents a group of variable; the size of the group is given as the value of this attribute. Typical use case: `CLASSES` will create a per-class variable in a global scope log |

_Inner text or CDATA:_ C expression that calculates the value of the variable from the current state of the TM. Required, unless `cache` attribute is given.

### &lt;event&gt;

_Attributes:_

| attribute | description |
| :--- | :--- |
| **id** | event name, also used as a column header in the CSV |
| **type** | (Optional, default is `int`) type of data: `int`, `long`, `float`, or `double` |
| **counter** | (Optional, used **id** by default) event counter field name |
| **reset** | (Optional, default is `0`) the counter is reset to this value every time it is logged |
| **avg** | (Optional) if specified, the value of the counter is averaged over this variable or parameter name |
