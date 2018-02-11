#!/usr/bin/env escript

-define(PORT, 4891).
-define(TIMEOUT, 3000).

main(_) -> io:format("listening to port ~w~n", [?PORT]), server().

server() ->
    case gen_tcp:listen(?PORT, [binary, {active, false}, {reuseaddr, true}, {nodelay, true}]) of
        {ok, LSock} -> wait_connect(LSock);
        E -> io:format("error occured: ~w~n", [E])
    end.

wait_connect(LSock) ->
    % io:format("trying to accept..."),
    case gen_tcp:accept(LSock) of
        {ok, Sock} ->
            Pid = spawn(
                fun () ->
                    receive start -> ok end,
                    get_request(Sock)
                end
            ),
            gen_tcp:controlling_process(Sock, Pid),
            Pid ! start;
        {error, Error} ->
            io:format("failed to accept: ~w~n", [Error]);
        E -> io:format("error: ~w~n", [E])
    end,
    wait_connect(LSock).

get_request(Sock) ->
    % io:format("get request~n"),
    send_response(Sock).

send_response(Sock) ->
    gen_tcp:send(Sock, "$1a\r\n"),
    gen_tcp:close(Sock).

