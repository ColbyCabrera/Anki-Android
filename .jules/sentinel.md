## 2025-02-18 - Partial Path Traversal in Zip Extraction
**Vulnerability:** The `zipPathSafety` check in `TgzPackageExtract.kt` used `startsWith` to check if the output path is within the destination directory. This allows writing to sibling directories with a matching prefix (e.g., `/dest` vs `/dest-suffix`).
**Learning:** `startsWith` on file paths is insufficient without ensuring a directory separator follows. Canonical paths simplify `..` but don't resolve the prefix issue automatically.
**Prevention:** Always append `File.separator` to the destination path when checking `startsWith`, or use `Path.relativize` and check for `..`.
