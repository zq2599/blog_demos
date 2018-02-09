
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "http11_parser.h"
#include <ctype.h>

#define BUFF_LEN 4096

void http_field(void *data, const char *field, 
        size_t flen, const char *value, size_t vlen)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, field, flen);
    strcat(buff, ": ");
    strncat(buff, value, vlen);

    printf("HEADER: \"%s\"\n", buff);
}

void request_method(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("METHOD: \"%s\"\n", buff);
}

void request_uri(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("URI: \"%s\"\n", buff);
}

void fragment(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("FRAGMENT: \"%s\"\n", buff);
}

void request_path(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("PATH: \"%s\"\n", buff);
}

void query_string(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("QUERY: \"%s\"\n", buff);
}

void http_version(void *data, const char *at, size_t length)
{
    char buff[BUFF_LEN] = {0};

    strncpy(buff, at, length);

    printf("VERSION: \"%s\"\n", buff);
}

void header_done(void *data, const char *at, size_t length)
{
    printf("done.\n");
}

void parser_init(http_parser *hp) 
{
    hp->http_field = http_field;
    hp->request_method = request_method;
    hp->request_uri = request_uri;
    hp->fragment = fragment;
    hp->request_path = request_path;
    hp->query_string = query_string;
    hp->http_version = http_version;
    hp->header_done = header_done;
    http_parser_init(hp);
}

int main1 ()
{
    char *data = "GET / HTTP/1.0\r\n" 
        "User-Agent: Wget/1.11.4\r\n" 
        "Accept: */*\r\n"
        "Host: www.163.com\r\n"
        "Connection: Keep-Alive\r\n"
        "\r\n";
    size_t dlen;
    http_parser parser, *hp;

    hp = &parser;
    dlen = strlen(data);

    parser_init(hp);

    http_parser_execute(hp, data, dlen, 0);

    return 0;
}

int main ()
{
    char *data = "GET / HTTP/1.0\r\n" 
        "User-Agent: Wget/1.11.4\r\n" 
        "Accept: */*\r\n"
        "Host: www.163.com\r\n"
        "Connection: Keep-Alive\r\n"
        "\r\n";


    size_t dlen, dlen1;
    http_parser parser, *hp;
    int i;

    hp = &parser;
    dlen = strlen(data);

    for (i = 1;i < dlen;i++) {
        parser_init(hp);
        dlen1 = http_parser_execute(hp, data, i, 0);
        dlen1 = http_parser_execute(hp, data, dlen, dlen1);
    }

    return 0;
}
