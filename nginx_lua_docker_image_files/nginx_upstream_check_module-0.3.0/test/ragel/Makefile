default: server client

server:  http11_parser.rl ragel_http_server.c 
	ragel -G2 http11_parser.rl
	gcc -g -Wall ragel_http_server.c http11_parser.c -o server


client:  http11_response.rl ragel_http_client.c 
	ragel -G2 http11_response.rl
	gcc -g -Wall ragel_http_client.c http11_response.c -o client 

clean: 
	rm client server
