--[[
// 1. Refresh own heartbeat at heartbeaterHb and refresh list
// 2. Compete for leader
// 3. depends on #2 result:
//    a. if is leader,(optional: extend the leader expiry time); get the list
//       iterate the list and find out who has no heartbeat
//       reQueue the message whose owner lost heartbeat
//    b. if not leader, return
--]]
local result = {}
-- Refresh own heartbeat
-- key1 heartbeat key: name:id:HB; arg1: same as key; arg2: expire time in sec
redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])

-- refresh the all consumers list
-- key2: list key; arg3: expiry time in sec
redis.call('EXPIRE', KEYS[2], ARGV[3])

-- compete for leader of all consumers (if no leader is set)
-- key3: leader key; arg4: name:id;  arg5: leader expire time
redis.call('SET', KEYS[3], ARGV[4], 'NX', 'EX', ARGV[5])

local isLeader = (redis.call('GET', KEYS[3]) == ARGV[4])
result[1] = isLeader

if isLeader then
	-- I am the leader
	-- get size of consumers in the list
	local size = redis.call('LLEN', KEYS[2])
	local orphanedConsumers = {}

	for i=1, size do
		-- pop one consumer from list and check its heartbeat
		local consumer = redis.call('RPOP', KEYS[2])
		-- arg6, hb postfix ":HB"
		if (not redis.call('GET', consumer..ARGV[6])) then
			-- orphaned consumer; pop the single message
			local orphanMsg = redis.call('LPOP', consumer)
			if orphanMsg then
				orphanedConsumers[#orphanedConsumers+1] = {consumer, 'true'}
				-- push back to head of wait queue
				redis.call('RPUSH', KEYS[4], orphanMsg)
			else
				-- no unacknowledged msg owned by this orphaned consumer
				orphanedConsumers[#orphanedConsumers+1] = {consumer, 'false'}
			end
		else
			-- active consumer, put it back to the list
			redis.call('LPUSH', KEYS[2], consumer)
		end	
	end
	result[2] = orphanedConsumers
	result[3] = size
	-- refresh the all consumers list
	redis.call('EXPIRE', KEYS[2], ARGV[3])
else
	result[2] = {}
	result[3] = -1
end

return result