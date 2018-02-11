
/*
 * Copyright (C) agentzh
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "rds_parser.h"

#include <lua.h>
#include <lauxlib.h>
#include <stdlib.h>
#include <string.h>


#define LUA_RDS_PARSER_VERSION "0.05"


static int rds_parse(lua_State *L);
static int rds_parse_header(lua_State *L, rds_buf_t *b, rds_header_t *header);
static int rds_parse_col(lua_State *L, rds_buf_t *b, rds_column_t *col);
static int rds_parse_row(lua_State *L, rds_buf_t *b, rds_header_t *header,
        rds_column_t *cols, int row);
static int rds_parse_field(lua_State *L, rds_buf_t *b, rds_header_t *header,
        rds_column_t *cols, int col, int row);


static char *rds_null = NULL;


static const struct luaL_Reg rds_parser[] = {
    {"parse", rds_parse},
    {NULL, NULL}
};


int
luaopen_rds_parser(lua_State *L)
{
    luaL_register(L, "rds.parser", rds_parser);

    lua_pushliteral(L, LUA_RDS_PARSER_VERSION);
    lua_setfield(L, -2, "_VERSION");

    lua_pushlightuserdata(L, rds_null);
    lua_setfield(L, -2, "null");

    return 1;
}


static int
rds_parse(lua_State *L)
{
    rds_buf_t           b;
    size_t              len;
    int                 rc;
    rds_header_t        h;
    rds_column_t       *cols;
    int                 i;

    luaL_checktype(L, 1, LUA_TSTRING);

    b.start = (u_char *) lua_tolstring(L, 1, &len);
    b.end = b.start + len;

    b.pos = b.start;
    b.last = b.end;

    rc = rds_parse_header(L, &b, &h);
    if (rc != 0) {
        return rc;
    }

    cols = lua_newuserdata(L, h.col_count * sizeof(rds_column_t));

    for (i = 0; i < h.col_count; i++) {
        rc = rds_parse_col(L, &b, &cols[i]);
        if (rc != 0) {
            return rc;
        }

        dd("pushing col name onto the stack, top %d", lua_gettop(L));

        lua_pushlstring(L, (char *) cols[i].name.data, cols[i].name.len);
    }

    lua_createtable(L, 0 /* narr */, 4 /* nrec */);

    lua_pushinteger(L, h.std_errcode);
    lua_setfield(L, -2, "errcode");

    if (h.errstr.len > 0) {
        lua_pushlstring(L, (char *) h.errstr.data, h.errstr.len);
        lua_setfield(L, -2, "errstr");
    }

    if (h.insert_id) {
        lua_pushinteger(L, h.insert_id);
        lua_setfield(L, -2, "insert_id");
    }

    if (h.affected_rows) {
        lua_pushinteger(L, h.affected_rows);
        lua_setfield(L, -2, "affected_rows");
    }

    if (h.col_count == 0) {
        return 1;
    }

    dd("creating resultset, top %d", lua_gettop(L));

    lua_newtable(L);

    for (i = 0; ; i++) {
        rc = rds_parse_row(L, &b, &h, cols, i);
        if (rc == -2) {
            continue;
        }

        if (rc == 0) {
            break;
        }

        return rc;
    }

    dd("saving resultset, top %d", lua_gettop(L));
    dd("-1: %s", luaL_typename(L, -1));
    dd("-2: %s", luaL_typename(L, -2));
    dd("-3: %s", luaL_typename(L, -3));

    lua_setfield(L, -2, "resultset");

    dd("returning %s", luaL_typename(L, -1));

    return 1;
}


static int
rds_parse_row(lua_State *L, rds_buf_t *b, rds_header_t *header,
        rds_column_t *cols, int row)
{
    int         col;
    int         rc;

    dd("parsing row %d, top %d", row + 1, lua_gettop(L));

    if (b->last - b->pos < (ssize_t) sizeof(uint8_t)) {
        lua_pushnil(L);
        lua_pushliteral(L, "row flag is incomplete");
        return 2;
    }

    if (*b->pos++ == 0) {
        if (b->pos != b->last) {
            lua_pushnil(L);
            lua_pushfstring(L, "seen unexpected leve-over data bytes "
                    "at offset %d, row %d",
                    (int) (b->pos - b->start), row + 1);
            return 2;
        }

        return 0;
    }

    dd("creating row table, top %d", lua_gettop(L));

    lua_createtable(L, 0, header->col_count /* nrec */);

    for (col = 0; col < header->col_count; col++) {
        rc = rds_parse_field(L, b, header, cols, col, row);
        if (rc == 0) {
            continue;
        }

        return rc;
    }

    dd("saving row table, top %d", lua_gettop(L));

    lua_rawseti(L, -2, row + 1);

    return -2;
}


static int
rds_parse_field(lua_State *L, rds_buf_t *b, rds_header_t *header,
        rds_column_t *cols, int col, int row)
{
    size_t          len;
    lua_Number      num;
    lua_Integer     integer;

    dd("parsing field at row %d, col %d, top %d", row + 1, col + 1,
            lua_gettop(L));

    if (b->last - b->pos < (ssize_t) sizeof(uint32_t)) {
        lua_pushnil(L);
        lua_pushfstring(L, "field size is incomplete at offset %d, row %d, "
                "col %d", (int) (b->pos - b->start), row + 1, col + 1);

        return 2;
    }

    len = *(uint32_t *) b->pos;

    b->pos += sizeof(uint32_t);

    /* push the key */
    lua_pushvalue(L, col + 3);

    if (len == (uint32_t) -1) {
        /* SQL NULL found */
        //lua_pushlightuserdata(L, rds_null);
        lua_pushlightuserdata(L, rds_null);

    } else {
        if (b->last - b->pos < (ssize_t) len) {
            lua_pushnil(L);
            lua_pushfstring(L, "field value is incomplete at offset %d, row %d,"
                    " col %d", (int) (b->pos - b->start), row + 1, col + 1);
            return 2;
        }

        switch (cols[col].std_type & 0xc000) {
        case rds_rough_col_type_float:
            lua_pushlstring(L, (char *) b->pos, len);
            num = lua_tonumber(L, -1);
            lua_pop(L, 1);
            lua_pushnumber(L, num);
            break;

        case rds_rough_col_type_int:
            lua_pushlstring(L, (char *) b->pos, len);
            integer = lua_tointeger(L, -1);
            lua_pop(L, 1);
            lua_pushinteger(L, integer);
            break;

        case rds_rough_col_type_bool:
            if (*b->pos == '0' || *b->pos == 'f' || *b->pos == 'F')
            {
                lua_pushboolean(L, 0);
                break;
            }

            if (*b->pos == '1' || *b->pos == 't' || *b->pos == 'T' )
            {
                lua_pushboolean(L, 1);
                break;
            }

            lua_pushnil(L);
            lua_pushfstring(L, "unrecognized boolean value at offset %d, "
                    "row %d, col %d", (int) (b->pos - b->start), row + 1,
                    col + 1);
            return 2;

        default:
            lua_pushlstring(L, (char *) b->pos, len);
            break;
        }

        b->pos += len;
    }

    dd("saving field, top %d", lua_gettop(L));
    lua_rawset(L, -3);

    return 0;
}


static int
rds_parse_header(lua_State *L, rds_buf_t *b, rds_header_t *header)
{
    ssize_t          rest;

    rest = sizeof(uint8_t)      /* endian type */
         + sizeof(uint32_t)     /* format version */
         + sizeof(uint8_t)      /* result type */

         + sizeof(uint16_t)     /* standard error code */
         + sizeof(uint16_t)     /* driver-specific error code */

         + sizeof(uint16_t)     /* driver-specific errstr len */
         + 0                    /* driver-specific errstr data */
         + sizeof(uint64_t)     /* affected rows */
         + sizeof(uint64_t)     /* insert id */
         + sizeof(uint16_t)     /* column count */
         ;

    if (b->last - b->pos < rest) {
        lua_pushnil(L);
        lua_pushliteral(L, "header part is incomplete");
        return 2;
    }

    /* TODO check endian type */

    b->pos += sizeof(uint8_t);

    /* check RDS format version number */

    if ( *(uint32_t *) b->pos != (uint32_t) resty_dbd_stream_version) {
        lua_pushnil(L);
        lua_pushfstring(L, "found RDS format version %d, "
                "but we can only handle version %d",
                (int) *(uint32_t *) b->pos, resty_dbd_stream_version);
        return 2;
    }

    dd("RDS format version: %d", (int) *(uint32_t *) b->pos);

    b->pos += sizeof(uint32_t);

    /* check RDS result type */

    if (*b->pos != 0) {
        lua_pushnil(L);
        lua_pushfstring(L, "RDS result type must be 0 for now but got %d",
                (int) *b->pos);
        return 2;
    }

    b->pos++;

    /* save the standard error code */

    header->std_errcode = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    /* save the driver-specific error code */

    header->drv_errcode = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    /* save the error string length */

    header->errstr.len = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    dd("errstr len: %d", (int) header->errstr.len);

    /* check the rest data's size */

    rest = header->errstr.len
         + sizeof(uint64_t)     /* affected rows */
         + sizeof(uint64_t)     /* insert id */
         + sizeof(uint16_t)     /* column count */
         ;

    if (b->last - b->pos < rest) {
        lua_pushnil(L);
        lua_pushliteral(L, "header part is incomplete");
        return 2;
    }

    /* save the error string data */

    header->errstr.data = b->pos;

    b->pos += header->errstr.len;

    /* save affected rows */

    header->affected_rows = *(uint64_t *) b->pos;

    b->pos += sizeof(uint64_t);

    /* save insert id */

    header->insert_id = *(uint64_t *)b->pos;

    b->pos += sizeof(uint64_t);

    /* save column count */

    header->col_count = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    dd("saved column count: %d", (int) header->col_count);

    return 0;
}


static int
rds_parse_col(lua_State *L, rds_buf_t *b, rds_column_t *col)
{
    ssize_t         rest;

    rest = sizeof(uint16_t)         /* std col type */
         + sizeof(uint16_t)         /* driver col type */
         + sizeof(uint16_t)         /* col name str len */
         ;

    if (b->last - b->pos < rest) {
        lua_pushnil(L);
        lua_pushliteral(L, "column spec is incomplete");
        return 2;
    }

    /* save standard column type */
    col->std_type = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    /* save driver-specific column type */
    col->drv_type = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    /* read column name string length */

    col->name.len = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    if (col->name.len == 0) {
        lua_pushnil(L);
        lua_pushliteral(L, "column name empty");
        return 2;
    }

    rest = col->name.len;

    if (b->last - b->pos < rest) {
        lua_pushnil(L);
        lua_pushliteral(L, "column name string is incomplete");
        return 2;
    }

    col->name.data = b->pos;

    b->pos += col->name.len;

    dd("saved column name \"%.*s\" (len %d, offset %d)",
            (int) col->name.len, col->name.data,
            (int) col->name.len, (int) (b->pos - b->start));

    return 0;
}

