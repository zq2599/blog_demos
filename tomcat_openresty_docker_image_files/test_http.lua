ngx.say("---------------------------------------------------------<br/>");

local http = require("resty.http")
--创建http客户端实例
local httpc = http.new()

local resp, err = httpc:request_uri("http://127.0.0.1:80",{
	method = "GET",
	path = "/tomcat_proxy/getserverinfo",
	headers = {
		["User-Agent"] = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36"
	}
})

if not resp then
	ngx.say("request error :", err)
	return
end


ngx.say("resp status--", resp.status)

ngx.say("<br/><br/>resp body--", resp.body)

httpc:close()






