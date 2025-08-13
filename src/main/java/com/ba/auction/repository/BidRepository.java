package com.ba.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ba.auction.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, Long> {

	Optional<Bid> findTopByAuctionedProductIdOrderByAmountDesc(Long auctionedProductId);

	List<Bid> findByAuctionedProductId(Long auctionedProductId);
}
