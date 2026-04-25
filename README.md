# Java CRM System — Control-File-Driven Client Management

A command-line **Customer Relationship Management (CRM)** system written in Java that reads a plain-text, XML-like control file and performs full CRUD (Create, Retrieve, Update, Delete) operations on a serialized client database. The program decodes Base64-encoded field values at runtime, giving control files a layer of encoding that separates raw user data from the script format. Built as a data structures project for CSC223, it demonstrates key ADT concepts including `HashMap` for O(1) record lookup, object serialization for persistence, and a comparator chaining strategy for multi field sorting. The system is designed for batch execution: one control file drives an entire workflow from database initialization through record management to program exit.

---

## Features

- **Create** new client records with five fields: CRMID, Surname, Given Name, Telephone, and Post Code
- **Retrieve** one or more clients into a search buffer by CRMID or field criteria
- **Output** the in-memory database (all records or retrieved subset) to the console in a formatted table
- **Update** individual fields of an existing client by CRMID without overwriting unchanged fields
- **Delete** client records by exact CRMID, by field-matching criteria, or all records at once using the reserved value `All`
- **Sort** output by one or more fields with cumulative, priority-chained ordering (surname → given name → telephone → post code)
- **Persist** the database to a binary `.ser` file via `<DBFileName>` and automatically reload it on the next run
- **Trace mode** for debugging — enabled via a `<Trace>` tag, prints `[TRACE]` logs for every internal operation
- **Comment support** — inline `#` line comments and `<Comment>` block tags for annotating control files
- **Graceful error handling** — unknown CRMIDs, invalid tags, and missing records produce descriptive console messages without crashing the program

---

## Project Structure

```
TCC-223-Control-file/
├── Scripts/
│   ├── CRM.java                    # Entry point; reads and parses the control file line by line
│   ├── ClientManager.java          # All CRUD logic, serialization, sort, and output formatting
│   ├── Client.java                 # Serializable POJO with five string fields
│   └── ClientComparator.java       # Multi-field Comparator<Client> for chained sorting
└── tests/
    ├── test_create.txt             # Create (full fields), Output all
    ├── test_update.txt             # Update single field + multi-field by CRMID
    ├── test_delete_by_id.txt       # Delete by exact CRMID + not-found error case
    ├── test_delete_all.txt         # Delete by field criteria + Delete All
    ├── test_retrieve.txt           # Retrieve by CRMID then Output subset vs. all
    ├── test_sort.txt               # Single-field sort + multi-field cumulative sort
    ├── test_persist_write.txt      # Create + Save (run before test_persist_read.txt)
    ├── test_persist_read.txt       # Load from disk + Output (run after test_persist_write.txt)
    ├── test_trace.txt              # Trace ON/OFF with visible [TRACE] log comparison
    ├── test_system_delete.txt      # System Delete removes physical file; in-memory data persists
    ├── test_errors.txt             # Missing fields, unknown CRMID, lowercase tag (all error paths)
    ├── sampleControlFile_corrected.txt   # Full lifecycle reference: Create×2, Retrieve, Output, Update, Delete, Exit
    └── sampleControlFile.txt
```

---

## How to Run

> **Prerequisites:** Java Development Kit (JDK) 8 or later. No build tools (Maven/Gradle) are required.

### 1. Compile

Run from the project root directory:

```bash
javac -d out Scripts/*.java
```

### 2. Execute Against a Control File

```bash
java -cp out Scripts.CRM <control_file.txt>
```

### 3. Examples

```bash
# Run individual feature tests
java -cp out Scripts.CRM tests/test_create.txt
java -cp out Scripts.CRM tests/test_update.txt
java -cp out Scripts.CRM tests/test_delete_by_id.txt
java -cp out Scripts.CRM tests/test_delete_all.txt
java -cp out Scripts.CRM tests/test_retrieve.txt
java -cp out Scripts.CRM tests/test_sort.txt
java -cp out Scripts.CRM tests/test_trace.txt
java -cp out Scripts.CRM tests/test_system_delete.txt
java -cp out Scripts.CRM tests/test_errors.txt

# Persistence: must run write first, then read (in order)
java -cp out Scripts.CRM tests/test_persist_write.txt
java -cp out Scripts.CRM tests/test_persist_read.txt

# Full lifecycle reference workflow
java -cp out Scripts.CRM tests/sampleControlFile_corrected.txt
```

---

## Control File Format

Control files use XML-like block tags. Field values (except `<CRMID>`) must be **Base64-encoded**. Tags are **case-sensitive**.

```
<System>
  <DBFileName>
  Q1NDLTIyMy1DUk1fZGF0YWJhc2U=      # Base64 for "CSC-223-CRM_database"
  </DBFileName>
</System>

<Create>
  <Surname>U21pdGg=</Surname>          # Base64 for "Smith"
  <GivenName>Sm9obg==</GivenName>      # Base64 for "John"
  <Telephone>NTU1LTEyMzQ=</Telephone>  # Base64 for "555-1234"
  <PostCode>MTIzNDU=</PostCode>        # Base64 for "12345"
</Create>
```

### Supported Block Tags

| Block Tag    | Purpose                                                              |
| :----------- | :------------------------------------------------------------------- |
| `<System>`   | Database config, Output, Trace, Comment, Exit, and file-level Delete |
| `<Create>`   | Add a new client record to the in-memory database                    |
| `<Retrieve>` | Load matching clients into the search buffer                         |
| `<Update>`   | Modify one or more fields of an existing client by CRMID             |
| `<Delete>`   | Remove client records by CRMID or field criteria                     |
| `<Sort>`     | Define the sort-field priority for subsequent `<Output>` calls       |

### Reserved CRMID Values

| Value  | Meaning                                  |
| :----- | :--------------------------------------- |
| `All`  | Targets all records (Output / Delete)    |
| `NULL` | Equivalent to `All` in Output and Delete |

---

## Input & Output Specifications

### Success Cases

| Operation              | Example Block Tag                                       | Expected Console Output                                      |
| :--------------------- | :------------------------------------------------------ | :----------------------------------------------------------- |
| **Create**             | `<Create>` with Surname, GivenName, Telephone, PostCode | `Client created: [CRMID] Surname, GivenName`                 |
| **Output (all)**       | `<Output>` with CRMID `All` inside `<System>`           | Formatted table of all client records in the database        |
| **Output (retrieved)** | `<Retrieve>` → `<Output>`                               | Formatted table showing only the retrieved subset            |
| **Update**             | `<Update>` with a valid CRMID and one or more fields    | `Client updated: [CRMID]` with the new field value confirmed |
| **Delete by CRMID**    | `<Delete><CRMID>TEST-001</CRMID></Delete>`              | `Client deleted: TEST-001`                                   |
| **Delete All**         | `<Delete><CRMID>All</CRMID></Delete>`                   | Each record confirmed deleted; database is empty             |
| **Sort**               | `<Sort><Surname>...</Surname></Sort>`                   | `<Output>` results printed in sorted order                   |
| **Exit**               | `<Exit>Base64Message</Exit>`                            | Decoded message printed; program terminates cleanly          |
| **Trace ON**           | `<Trace>T04=</Trace>` (`"ON"`) inside `<System>`        | `[TRACE]` log lines printed for every subsequent operation   |
| **Persist (Save)**     | `<DBFileName>` in `<System>` block                      | Database saved to `.ser` file; confirmed on console          |
| **Persist (Load)**     | `<DBFileName>` on a subsequent run                      | Previously saved records loaded back into memory             |

### Error Handling

| Invalid Input                                                                       | What the Program Does                                                                                      |
| :---------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| CRMID not found during `<Update>`                                                   | Prints an error message (e.g., `Error: Client not found — [CRMID]`); skips the update; continues execution |
| CRMID not found during `<Delete>`                                                   | Prints a not-found warning; no record is removed; execution continues                                      |
| Lowercase or unrecognized block tag (e.g., `<create>`, `<system>`)                  | Tag is **silently ignored**; no crash; processing resumes on the next line                                 |
| Malformed Base64 value                                                              | Decoding produces an empty or garbage string; the operation proceeds with that empty value                 |
| `<Delete>` inside `<System>` block with no database file present                    | Warning printed; no file to delete; execution continues                                                    |
| `<Output>` called before any `<DBFileName>` is set                                  | Outputs an empty database with a header; no crash                                                          |
| `<Retrieve>` with no matching records                                               | Search buffer remains empty; a subsequent `<Output>` prints nothing for that buffer                        |
| `#` comment line anywhere in the file                                               | Line is skipped entirely; no effect on state                                                               |
| Field tag outside of a recognized block (e.g., `<Surname>` with no open `<Create>`) | Tag is ignored; buffer is not populated                                                                    |

---

## Architecture Overview

The program is structured in three layers:

1. **`CRM.java`** — Parses the control file line by line, tracking the active block (`currentBlock`) and active field tag (`currentTag`). On each closing tag it decodes the buffer and dispatches to `ClientManager`.

2. **`ClientManager.java`** — Owns all runtime state: the `HashMap<String, Client>` in-memory database, temp buffers for in-progress create/update operations, search buffers for retrieve/delete, and the sort-order list. All CRUD methods, serialization (load/save/delete), output formatting, and trace logging live here.

3. **`Client.java`** — A plain serializable POJO with five `String` fields: `crmID`, `surname`, `givenName`, `telephone`, `postCode`. Contains no business logic.

4. **`ClientComparator.java`** — A `Comparator<Client>` that chains multiple sort fields in priority order. The first non-zero comparison result wins, enabling cumulative multi-field sorting.

---

## Testing

Each test file is self-contained and targets one specific feature. Run them individually from the project root after compiling.

| Test File                      | Feature Covered                       | Key Assertion                                                                 |
| :----------------------------- | :------------------------------------ | :---------------------------------------------------------------------------- |
| `tests/test_create.txt`        | `<Create>` with all 5 fields          | 3 clients appear in the Output table                                          |
| `tests/test_update.txt`        | `<Update>` single field + multi-field | GivenName, then Surname/Telephone/PostCode change independently               |
| `tests/test_delete_by_id.txt`  | `<Delete>` by exact CRMID             | Database is empty after delete; error message on non-existent CRMID           |
| `tests/test_delete_all.txt`    | Delete by field criteria + Delete All | 2 Smiths removed by surname; final Delete All empties the database            |
| `tests/test_retrieve.txt`      | `<Retrieve>` by CRMID + Output All    | Retrieve shows 1 row; Output All shows all 3 rows                             |
| `tests/test_sort.txt`          | Single-field + multi-field `<Sort>`   | Jones/Anna appears before Jones/Bob only in multi-field sort                  |
| `tests/test_persist_write.txt` | `<Save>` to binary database file      | `[CRM] Database saved: CSC223-PERSIST-DB (2 records)`                         |
| `tests/test_persist_read.txt`  | Load from disk on startup             | `[CRM] Database loaded: CSC223-PERSIST-DB (2 records)` — **run after write**  |
| `tests/test_trace.txt`         | `<Trace>` ON/OFF                      | `[TRACE]` lines present when ON; absent when OFF                              |
| `tests/test_system_delete.txt` | System `<Delete>` removes file        | Second `<DBFileName>` load prints "Starting fresh." (file is gone)            |
| `tests/test_errors.txt`        | All error paths                       | Missing field warning; CRMID-not-found errors; lowercase tag silently ignored |

> **Testing note:** If `Surname` is set to `TEST_OVERRIDE`, the system assigns the fixed CRMID `TEST-001` instead of a random UUID. This is used in `test_update.txt`, `test_delete_by_id.txt`, and `test_retrieve.txt` to write predictable Update and Delete blocks without a manual ID lookup.
