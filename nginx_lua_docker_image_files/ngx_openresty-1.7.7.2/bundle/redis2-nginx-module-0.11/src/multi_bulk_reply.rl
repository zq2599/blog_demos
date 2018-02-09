%%{
    machine multi_bulk_reply;

    include common "common.rl";

    action test_chunk_count {
#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
    }

    action start_reading_chunk {
        dd("start reading bulk");
        ctx->chunks_read = 0;
    }

    action start_reading_count {
        dd("start reading bulk count");
        ctx->chunk_count = 0;
    }

    action read_count {
        ctx->chunk_count *= 10;
        ctx->chunk_count += *p - '0';
        dd("chunk count: %d", (int) ctx->chunk_count);
    }

    action multi_bulk_finalize {
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }

    reply = single_line_reply @read_chunk
          | chunk
          ;

    protected_chunk = reply when test_chunk_count
                    ;

    chunk_count = ([1-9] digit*) >start_reading_count $read_count
                ;

    multi_bulk_reply = "*" "-1" CRLF @multi_bulk_finalize
                     | "*" "0"+ CRLF @multi_bulk_finalize
                     | "*" chunk_count CRLF @start_reading_chunk
                        protected_chunk+
                        @multi_bulk_finalize
                     ;

}%%

