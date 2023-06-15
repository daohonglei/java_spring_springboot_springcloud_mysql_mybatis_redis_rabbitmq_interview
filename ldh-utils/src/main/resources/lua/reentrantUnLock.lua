if redis.call('exists', KEYS[1]) == 1 then
    if redis.call('hexists', KEYS[1], KEYS[2]) == 1 then
        if tonumber(redis.call('hincrby', KEYS[1], KEYS[2], -1)) <= 0 then
            redis.call('hdel', KEYS[1], KEYS[2])
            redis.call('del', KEYS[1])
            return 0
        else
            return 1
        end
    else
        return 0
    end
else
    return 0
end