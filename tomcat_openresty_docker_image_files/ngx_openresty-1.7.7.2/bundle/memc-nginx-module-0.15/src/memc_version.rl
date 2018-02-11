%%{
    machine memc_version;

    alphtype short;

    include memc_common "memc_common.rl";

    version = any+ - "\r\n"
            ;

    response = "VERSION " version "\r\n"
             | error
             ;

    main := response @finalize
         ;

}%%

