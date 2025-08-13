package com.ba.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ba.auction.entity.AuctionedProduct;

public interface AuctionedProductRepository extends JpaRepository<AuctionedProduct, Long> {

	List<AuctionedProduct> findByAuctionId(Long auctionId);

	Optional<AuctionedProduct> findByIdAndAuctionId(Long id, Long auctionId);

}
