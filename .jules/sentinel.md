## 2024-05-23 - Path Traversal in ViewerResourceHandler
**Vulnerability:** `ViewerResourceHandler` served files from the media directory based on `request.url.path` without verifying that the resolved file remained within the media directory. An attacker could potentially construct a path like `../../etc/passwd` to access files outside the intended scope.
**Learning:** Even when using `File(parent, child)`, `child` can contain traversal sequences that break out of `parent`. Always validate the `canonicalPath` of the resulting file.
**Prevention:** Ensure `file.canonicalPath.startsWith(parent.canonicalPath + File.separator)` before accessing the file.
