local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])
local currentLimit = tonumber(redis.call('get', key) or "0")
if currentLimit + 1 > limit
then
    return 0
else
    redis.call('INCRBY', key, 1)
    redis.call('EXPIRE', key, expire)
    return currentLimit + 1
end