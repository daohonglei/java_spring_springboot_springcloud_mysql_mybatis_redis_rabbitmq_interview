if redis.call('hexists', KEYS[1], KEYS[2]) == 1 then
    if tonumber(redis.call('hget',KEYS[1], KEYS[2])) >=1 then
        redis.call('hincrby', KEYS[1], KEYS[2], -1)
        return 1
    else
        return 0
    end
else
    return redis.call('hset',KEYS[1], KEYS[2], ARGV[1])
end