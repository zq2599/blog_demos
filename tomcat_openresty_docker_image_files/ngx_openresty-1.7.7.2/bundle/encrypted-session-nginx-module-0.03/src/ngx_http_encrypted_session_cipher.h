#ifndef NGX_HTTP_ENCRYPTED_SESSION_CIPHER_H
#define NGX_HTTP_ENCRYPTED_SESSION_CIPHER_H


#include <ngx_core.h>
#include <ngx_http.h>
#include <openssl/evp.h>


enum {
    ngx_http_encrypted_session_key_length = 256 / 8,
    ngx_http_encrypted_session_iv_length = EVP_MAX_IV_LENGTH
};


ngx_int_t ngx_http_encrypted_session_aes_mac_encrypt(ngx_pool_t *pool,
        ngx_log_t *log, const u_char *iv, size_t iv_len, const u_char *key,
        size_t key_len, const u_char *in, size_t in_len,
        ngx_uint_t expires, u_char **dst, size_t *dst_len);

ngx_int_t ngx_http_encrypted_session_aes_mac_decrypt(ngx_pool_t *pool,
        ngx_log_t *log, const u_char *iv, size_t iv_len, const u_char *key,
        size_t key_len, const u_char *in, size_t in_len, u_char **dst,
        size_t *dst_len);


#endif /* NGX_HTTP_ENCRYPTED_SESSION_CIPHER_H */

