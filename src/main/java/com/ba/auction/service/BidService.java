package com.ba.auction.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.ThreadContext;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.ba.auction.entity.AuctionedProduct;
import com.ba.auction.entity.Bid;
import com.ba.auction.model.BidDetails;
import com.ba.auction.repository.AuctionedProductRepository;
import com.ba.auction.repository.BidRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidRepository bidRepository;
	private final AuctionedProductRepository auctionedProductRepository;
	private final ModelMapper modelMapper;

	public void placeBid(Long auctionId, Long auctionproductId, BidDetails bidDetails) {
		AuctionedProduct product = auctionedProductRepository.findByIdAndAuctionId(auctionproductId, auctionId)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		
		if(bidDetails.getAmount() > product.getMinimumBid()) {
			Bid bid = modelMapper.map(bidDetails, Bid.class);
			bid.setAuctionedProduct(product);
			bid.setTimestamp(LocalDateTime.now());
			bid.setBidderUserId(ThreadContext.get("uid"));
			bidRepository.save(bid);
		}	
	}
}
