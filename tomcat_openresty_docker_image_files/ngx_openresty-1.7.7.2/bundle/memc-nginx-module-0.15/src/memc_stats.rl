%%{
    machine memc_stats;

    alphtype short;

    include memc_common "memc_common.rl";

    content = any+ - "\r\n"
            ;

    stat_line = "STAT " content "\r\n"
              ;

    response = stat_line* "END\r\n"
             | error
             ;

    main := response @finalize
         ;

}%%

