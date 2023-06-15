if redis.call('hexists', KEYS[1], KEYS[2]) == 1 then
    if tonumber(redis.call('ttl', KEYS[1])) <= tonumber(ARGV[1]) then
        return redis.call('expire', KEYS[1], ARGV[2])
    else
        return 1
    end
else
    return 0
end