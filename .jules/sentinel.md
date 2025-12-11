## 2025-02-12 - Zip Slip Sibling Directory Bypass
**Vulnerability:** Path traversal check `startsWith(dest)` allowed access to sibling directories sharing the prefix (e.g., `/dest_suffix`).
**Learning:** `startsWith` is purely string-based. Canonical paths do not automatically end in a separator.
**Prevention:** Ensure the check includes the separator: `startsWith(dest + File.separator)`.
