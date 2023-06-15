if redis.call('hexists', KEYS[1], KEYS[2]) == 1 then
    redis.call('hincrby', KEYS[1], KEYS[2], 1)
    return 1
else
    return redis.call('hset', KEYS[1], KEYS[2], 1)
end