local var = ngx.var
local i = var.a
local j = var.b
local sequare = require("sequare")
local s1 = sequare:new(i, j)

return s1:get_square() 
