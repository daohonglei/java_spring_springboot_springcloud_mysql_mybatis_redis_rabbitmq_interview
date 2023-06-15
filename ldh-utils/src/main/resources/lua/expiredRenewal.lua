if redis.call('get', KEYS[1]) == ARGV[1] then
    if tonumber(redis.call('ttl', KEYS[1])) <= tonumber(ARGV[2]) then
        return redis.call('expire', KEYS[1], ARGV[3])
    else
        return 1
    end
else
    return 0
end