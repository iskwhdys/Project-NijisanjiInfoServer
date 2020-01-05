package com.iskwhdys.project.domain.channel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

	/*
	public default int getNextOrderSeq(JdbcTemplate jdbcTemplate) {
		int seq = jdbcTemplate.queryForObject("SELECT nextval('public.order_no_seq')", Integer.class);
		return seq;
	}
	*/

}