# tools.github.alpha


``` shell
clojure -Sdeps '{:deps {ajchemist/tools.github.alpha {:git/url "https://github.com/ajchemist/tools.github.alpha" :sha "SHA"}}}' \
    -- \
    -m user.tools.github.alpha.script.repo-actions \
    list-secrets \
    --owner OWNER \
    --repo REPO \
    --basic-auth BASIC_AUTH
```
