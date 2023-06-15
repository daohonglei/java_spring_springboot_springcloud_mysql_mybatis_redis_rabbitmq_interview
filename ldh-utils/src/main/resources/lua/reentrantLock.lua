if redis.call('exists', KEYS[1]) == 1 then
    if redis.call('hexists', KEYS[1], KEYS[2]) == 1 then
        redis.call('hincrby', KEYS[1], KEYS[2], 1)
        return 1
    else
        return 0
    end
else
    redis.call('hset', KEYS[1], KEYS[2], 1)
    return redis.call('expire', KEYS[1], ARGV[1])
end