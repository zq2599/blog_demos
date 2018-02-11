-- Copyright (C) Yichun Zhang (agentzh)


require "resty.core.uri"
require "resty.core.hash"
require "resty.core.base64"
require "resty.core.regex"
require "resty.core.exit"
require "resty.core.shdict"
require "resty.core.var"
require "resty.core.ctx"
require "resty.core.misc"
require "resty.core.request"
require "resty.core.response"
require "resty.core.time"
require "resty.core.worker"


local base = require "resty.core.base"


return {
    version = base.version
}
