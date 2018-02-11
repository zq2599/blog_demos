%%{
    machine memc_storage;

    include memc_common "memc_common.rl";

    response = "STORED\r\n" @handle_stored
             | "NOT_STORED\r\n"
             | "EXISTS\r\n"
             | "NOT_FOUND\r\n" @handle_not_found
             | error
             ;

    main := response @finalize
         ;

}%%

