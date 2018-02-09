%%{
    machine memc_incr_decr;

    alphtype short;

    include memc_common "memc_common.rl";

    value = digit+
          ;

    response = value " "* "\r\n" @handle_stored
             | "NOT_FOUND\r\n" @handle_not_found
             | error
             ;

    main := response @finalize $check
         ;

}%%

