%%{
    machine memc_delete;

    alphtype short;

    include memc_common "memc_common.rl";

    response = "DELETED\r\n"
             | "NOT_FOUND\r\n" @handle_not_found
             | error
             ;

    main := response @finalize
         ;

}%%

