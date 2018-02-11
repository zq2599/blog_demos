#if defined(DDEBUG) && (DDEBUG)

#   include <stdio.h>

#   define dd(...) \
    fprintf(stderr, __VA_ARGS__); \
    fprintf(stderr, "\n")

#else

#   define dd(fmt, ...)

#endif

