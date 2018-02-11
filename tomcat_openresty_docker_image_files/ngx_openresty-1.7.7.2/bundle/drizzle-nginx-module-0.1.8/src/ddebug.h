#ifndef DDEBUG_H
#define DDEBUG_H

#include <ngx_core.h>

#if defined(DDEBUG) && (DDEBUG)

#   define dd_dump_chain_size() { \
        int              n; \
        ngx_chain_t     *cl; \
            \
        for (n = 0, cl = u->out_bufs; cl; cl = cl->next, n++) { \
        } \
            \
        dd("chain size: %d", n); \
    }

#   if (NGX_HAVE_VARIADIC_MACROS)

#       define dd(...) fprintf(stderr, "drizzle *** %s: ", __func__); \
            fprintf(stderr, __VA_ARGS__); \
            fprintf(stderr, " at %s line %d.\n", __FILE__, __LINE__)

#   else

#include <stdarg.h>
#include <stdio.h>

#include <stdarg.h>

static void dd(const char * fmt, ...) {
}

#    endif

#else

#   define dd_dump_chain_size()

#   if (NGX_HAVE_VARIADIC_MACROS)

#       define dd(...)

#   else

#include <stdarg.h>

static void dd(const char * fmt, ...) {
}

#   endif

#endif

#if defined(DDEBUG) && (DDEBUG)

#define dd_check_read_event_handler(r)   \
    dd("r->read_event_handler = %s", \
        r->read_event_handler == ngx_http_block_reading ? \
            "ngx_http_block_reading" : \
        r->read_event_handler == ngx_http_test_reading ? \
            "ngx_http_test_reading" : \
        r->read_event_handler == ngx_http_request_empty_handler ? \
            "ngx_http_request_empty_handler" : "UNKNOWN")

#define dd_check_write_event_handler(r)   \
    dd("r->write_event_handler = %s", \
        r->write_event_handler == ngx_http_handler ? \
            "ngx_http_handler" : \
        r->write_event_handler == ngx_http_core_run_phases ? \
            "ngx_http_core_run_phases" : \
        r->write_event_handler == ngx_http_request_empty_handler ? \
            "ngx_http_request_empty_handler" : "UNKNOWN")

#else

#define dd_check_read_event_handler(r)
#define dd_check_write_event_handler(r)

#endif

#define dd_drizzle_result(result) \
    dd("drizzle result:     row_count=%" PRId64 "\n" \
         "            insert_id=%" PRId64 "\n" \
         "        warning_count=%u\n" \
         "         column_count=%u\n\n", \
         drizzle_result_row_count(result), \
         drizzle_result_insert_id(result), \
         drizzle_result_warning_count(result), \
         drizzle_result_column_count(result))

#define dd_drizzle_column(column) \
    dd("drizzle column:   catalog=%s\n" \
         "              db=%s\n" \
         "           table=%s\n" \
         "       org_table=%s\n" \
         "            name=%s\n" \
         "        org_name=%s\n" \
         "         charset=%u\n" \
         "            size=%u\n" \
         "        max_size=%zu\n" \
         "            type=%u\n" \
         "           flags=%u\n\n", \
         drizzle_column_catalog(column), drizzle_column_db(column), \
         drizzle_column_table(column), drizzle_column_orig_table(column), \
         drizzle_column_name(column), drizzle_column_orig_name(column), \
         drizzle_column_charset(column), drizzle_column_size(column), \
         drizzle_column_max_size(column), drizzle_column_type(column), \
         drizzle_column_flags(column));

#endif /* DDEBUG_H */

