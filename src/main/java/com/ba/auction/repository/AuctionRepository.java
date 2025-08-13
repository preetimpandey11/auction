package com.ba.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ba.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

	List<Auction> findByEndedFalse();

	Optional<Auction> findByIdAndOwnerUid(Long auctionId, String ownerUid);
}
