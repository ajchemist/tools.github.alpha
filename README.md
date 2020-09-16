# tools.github.alpha


``` shell
clojure -Sdeps '{:deps {ajchemist/tools.github.alpha {:git/url "https://github.com/ajchemist/tools.github.alpha" :sha "75338a47308667240b6760992a0b5c04a76f0cf7"}}}' \
    -- \
    -m user.tools.github.alpha.script.repo-actions \
    list-secrets \
    --owner OWNER \
    --repo REPO \
    --basic-auth BASIC_AUTH
```
