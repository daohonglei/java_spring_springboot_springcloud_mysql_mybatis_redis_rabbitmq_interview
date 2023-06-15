if redis.call('hset', KEYS[1], KEYS[2], 1) == 1 then
    return redis.call('expire', KEYS[1], ARGV[1])
end