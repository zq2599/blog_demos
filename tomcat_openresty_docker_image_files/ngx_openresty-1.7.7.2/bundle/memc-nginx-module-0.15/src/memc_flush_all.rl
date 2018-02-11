%%{
    machine memc_flush_all;

    alphtype short;

    include memc_common "memc_common.rl";

    response = "OK\r\n"
             | error
             ;

    main := response @finalize
         ;

}%%

