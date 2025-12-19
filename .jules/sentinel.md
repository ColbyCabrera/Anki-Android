## 2024-05-23 - Command Injection in BackupManager
**Vulnerability:** `BackupManager.kt` used `Runtime.exec` with `sh -c` to execute a piped command string constructed with an unescaped file path (`colPath`).
**Learning:** Even if a binary (like `sqlite3`) is expected to be missing on modern devices, legacy code paths that attempt to shell out can expose critical vulnerabilities if the input path can be manipulated.
**Prevention:** Avoid `Runtime.exec` with `sh -c` and string concatenation for commands. Use `ProcessBuilder` with separate processes for each command in a pipeline, and manage standard I/O streams programmatically to connect them. This ensures arguments are treated as data, not executable shell code.
