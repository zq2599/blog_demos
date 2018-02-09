
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "http11_response.h"
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

void http_version(void *data, const char *at, size_t length)
{
    printf("VERSION: \"%.*s\"\n", length, at);
}

void status_code(void *data, const char *at, size_t length)
{
    printf("STATUS_CODE: \"%.*s\"\n", length, at);
}

void reason_phrase(void *data, const char *at, size_t length)
{
    printf("REASON_PHRASE: \"%.*s\"\n", length, at);
}

void header_done(void *data, const char *at, size_t length)
{
    printf("HEADER_DONE.\n");
}

void parser_init(http_parser *hp) 
{
    hp->http_field = http_field;
    hp->http_version = http_version;
    hp->status_code = status_code;
    hp->reason_phrase = reason_phrase;
    hp->header_done = header_done;
    http_parser_init(hp);
}

int main1 ()
{
    char *data = "HTTP/1.0 200 OK\r\n"
        "Server: nginx\r\n"
        "Date: Fri, 26 Mar 2010 03:39:03 GMT\r\n"
        "Content-Type: text/html; charset=GBK\r\n"
        "Vary: Accept-Encoding\r\n"
        "Expires: Fri, 26 Mar 2010 03:40:23 GMT\r\n"
        "Cache-Control: max-age=80\r\n"
        "Vary: User-Agent\r\n"
        "Vary: Accept\r\n"
        "X-Cache: MISS from cache.163.com\r\n"
        "Connection: close\r\n"
        "\r\n"
        "I am the body"
        ;
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
    char *data = "HTTP/1.0 200 OK\r\n"
        "Server: nginx\r\n"
        "Date: Fri, 26 Mar 2010 03:39:03 GMT\r\n"
        "Content-Type: text/html; charset=GBK\r\n"
        "Vary: Accept-Encoding\r\n"
        "Expires: Fri, 26 Mar 2010 03:40:23 GMT\r\n"
        "Cache-Control: max-age=80\r\n"
        "Vary: User-Agent\r\n"
        "Vary: Accept\r\n"
        "X-Cache: MISS from cache.163.com\r\n"
        "Connection: close\r\n"
        "\r\n"
        "I am the body"
        ;


    size_t dlen, dlen1;
    http_parser parser, *hp;
    int i;

    hp = &parser;
    dlen = strlen(data);

    for (i = 1;i < dlen;i++) {
        printf("\n\nblock point: %d\n", i);
        parser_init(hp);
        dlen1 = http_parser_execute(hp, data, i, 0);
        dlen1 = http_parser_execute(hp, data, dlen, dlen1);
        printf("BODY: \"%s\"\n", data + hp->body_start);
    }

    return 0;
}
