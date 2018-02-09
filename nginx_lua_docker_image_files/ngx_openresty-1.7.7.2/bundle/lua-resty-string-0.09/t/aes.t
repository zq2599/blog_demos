# vi:ft=

use Test::Nginx::Socket::Lua;

repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

our $HttpConfig = <<'_EOC_';
    lua_package_path 'lib/?.lua;;';
    lua_package_cpath 'lib/?.so;;';
_EOC_

#log_level 'warn';

run_tests();

__DATA__

=== TEST 1: AES default hello
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("secret")
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 CBC MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
        ';
    }
--- request
GET /t
--- response_body
AES-128 CBC MD5: 7b47a4dbb11e2cddb2f3740c9e3a552b
true
--- no_error_log
[error]



=== TEST 2: AES empty key hello
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("")
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 (empty key) CBC MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
        ';
    }
--- request
GET /t
--- response_body
AES-128 (empty key) CBC MD5: 6cb1a35bf9d66e92c9dec684fc329746
true
--- no_error_log
[error]



=== TEST 3: AES 8-byte salt
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("secret","WhatSalt")
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 (salted) CBC MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
        ';
    }
--- request
GET /t
--- response_body
AES-128 (salted) CBC MD5: f72db89f8e19326d8da4928be106705c
true
--- no_error_log
[error]



=== TEST 4: AES oversized 10-byte salt
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("secret","Oversized!")
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 (oversized salt) CBC MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
            local aes_check = aes:new("secret","Oversize")
            local encrypted_check = aes_check:encrypt("hello")
            ngx.say(encrypted_check == encrypted)
        ';
    }
--- request
GET /t
--- response_body
AES-128 (oversized salt) CBC MD5: 90a9c9a96f06c597c8da99c37a6c689f
true
true
--- no_error_log
[error]



=== TEST 5: AES-256 ECB SHA1 no salt
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("secret",nil,
              aes.cipher(256,"ecb"),aes.hash.sha1)
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-256 ECB SHA1: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
        ';
    }
--- request
GET /t
--- response_body
AES-256 ECB SHA1: 927148b31f0e89696a222489403f540d
true
--- no_error_log
[error]



=== TEST 6: AES-256 ECB SHA1x5 no salt
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("secret",nil,
              aes.cipher(256,"ecb"),aes.hash.sha1,5)
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-256 ECB SHA1: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
        ';
    }
--- request
GET /t
--- response_body
AES-256 ECB SHA1: d1a9b6e59b8980e783df223889563bee
true
--- no_error_log
[error]



=== TEST 7: AES-128 CBC custom keygen
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new("Xr4ilOzQ4PCOq3aQ0qbuaQ==",nil,
              aes.cipher(128,"cbc"),
              {iv = ngx.decode_base64("Jq5cyFTja2vfyjZoSN6muw=="),
               method = ngx.decode_base64})
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 CBC (custom keygen) MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
            local aes_check = aes:new("secret")
            local encrypted_check = aes_check:encrypt("hello")
            ngx.say(encrypted_check == encrypted)
        ';
    }
--- request
GET /t
--- response_body
AES-128 CBC (custom keygen) MD5: 7b47a4dbb11e2cddb2f3740c9e3a552b
true
true
--- no_error_log
[error]



=== TEST 8: AES-128 CBC custom keygen (without method)
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"
            local aes_default = aes:new(ngx.decode_base64("Xr4ilOzQ4PCOq3aQ0qbuaQ=="),nil,
              aes.cipher(128,"cbc"),
              {iv = ngx.decode_base64("Jq5cyFTja2vfyjZoSN6muw==")})
            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 CBC (custom keygen) MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
            local aes_check = aes:new("secret")
            local encrypted_check = aes_check:encrypt("hello")
            ngx.say(encrypted_check == encrypted)
        ';
    }
--- request
GET /t
--- response_body
AES-128 CBC (custom keygen) MD5: 7b47a4dbb11e2cddb2f3740c9e3a552b
true
true
--- no_error_log
[error]



=== TEST 9: AES-128 CBC custom keygen (without method, bad key len)
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"

            local aes_default, err = aes:new("hel", nil, aes.cipher(128,"cbc"),
              {iv = ngx.decode_base64("Jq5cyFTja2vfyjZoSN6muw==")})

            if not aes_default then
                ngx.say("failed to new: ", err)
                return
            end

            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 CBC (custom keygen) MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
            local aes_check = aes:new("secret")
            local encrypted_check = aes_check:encrypt("hello")
            ngx.say(encrypted_check == encrypted)
        ';
    }
--- request
GET /t
--- response_body
failed to new: bad key length
--- no_error_log
[error]



=== TEST 10: AES-128 CBC custom keygen (without method, bad iv)
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local aes = require "resty.aes"
            local str = require "resty.string"

            local aes_default, err = aes:new(
                ngx.decode_base64("Xr4ilOzQ4PCOq3aQ0qbuaQ=="),
                nil,
                aes.cipher(128,"cbc"),
                {iv = "hello"}
            )

            if not aes_default then
                ngx.say("failed to new: ", err)
                return
            end

            local encrypted = aes_default:encrypt("hello")
            ngx.say("AES-128 CBC (custom keygen) MD5: ", str.to_hex(encrypted))
            local decrypted = aes_default:decrypt(encrypted)
            ngx.say(decrypted == "hello")
            local aes_check = aes:new("secret")
            local encrypted_check = aes_check:encrypt("hello")
            ngx.say(encrypted_check == encrypted)
        ';
    }
--- request
GET /t
--- response_body
failed to new: bad iv
--- no_error_log
[error]

