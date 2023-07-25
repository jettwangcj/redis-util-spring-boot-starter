--生产速率，每秒生产多少个令牌
local rate = tonumber(ARGV[1])
--令牌桶容量
local capacity = tonumber(ARGV[2])
--当前时间（秒级时间戳）
local now = tonumber(ARGV[3])
--每个请求消耗的令牌个数 固定为 1
local requested = tonumber(ARGV[4])

--填充时间=容量/生产速率
local fill_time = capacity/rate
--key过期时间设置为填充时间的2倍
local ttl = math.floor(fill_time*2)

-- 获取剩余令牌数量 , KEYS[1] redis key名，用于保存限流维度下剩余令牌数量，request_rate_limiter.{id}.tokens
local last_tokens = tonumber(redis.call("get", KEYS[1]))
--不存在key，则初始化令牌数量为最大容量,也就是说的 初始化时令牌数为桶容量
if last_tokens == nil then
  last_tokens = capacity
end

--最近获取令牌秒级时间戳
local last_refreshed = tonumber(redis.call("get", KEYS[2]))
if last_refreshed == nil then
  last_refreshed = 0
end

--距离上次获取令牌时间相差多少秒
local delta = math.max(0, now-last_refreshed)

--计算当前令牌数量（考虑delta时间内生成的令牌个数=delta*速率），取 容量和 剩余令牌+生成令牌数 的最小值，也就是说，达到容量后，就令牌就丢弃了
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))

--当前令牌数量是否大于1
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0

--允许访问，新令牌数量-1，allowed_num=1
if allowed then
  new_tokens = filled_tokens - requested
  allowed_num = 1
end

--保存令牌个数和最近获取令牌时间
redis.call("setex", KEYS[1], ttl, new_tokens)
redis.call("setex", KEYS[2], ttl, now)

return allowed_num
