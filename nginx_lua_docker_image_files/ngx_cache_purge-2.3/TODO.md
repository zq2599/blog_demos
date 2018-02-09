Features that __will not__ be added to `ngx_cache_purge`:

* Support for prefixed purges (`/purge/images/*`).  
  Reason: Impossible with current cache implementation.

* Support for wildcard/regex purges (`/purge/*.jpg`).  
  Reason: Impossible with current cache implementation.
