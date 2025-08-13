package com.ba.auction.service;

import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.ba.auction.entity.Auction;
import com.ba.auction.entity.AuctionedProduct;
import com.ba.auction.model.AuctionClosureDetails;
import com.ba.auction.model.AuctionClosureDetailsProductsInner;
import com.ba.auction.model.AuctionDetails;
import com.ba.auction.repository.AuctionRepository;
import com.ba.auction.repository.AuctionedProductRepository;
import com.ba.auction.repository.BidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionRepository auctionRepository;

	private final AuctionedProductRepository auctionedProductRepository;

	private final BidRepository bidRepository;

	private final ModelMapper modelMapper;

	public Auction getAuctionForOwner(Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new IllegalArgumentException("Auction not found"));

		if (!auction.getOwnerUid().equals(ThreadContext.get("uid"))) {
			throw new AccessDeniedException("You are not allowed to access this auction");
		}

		return auction;
	}

	public List<Auction> getAllActiveAuctions() {
		return auctionRepository.findByEndedFalse();
	}

	public AuctionClosureDetails endAuction(Long auctionId) {
		Auction auction = getAuctionForOwner(auctionId);
		if (auction.isEnded()) {
			throw new RuntimeException("Auction already ended");
		}

		List<AuctionedProduct> auctionedProducts = auctionedProductRepository.findByAuctionId(auctionId);

		auctionedProducts.forEach(ap -> {
			bidRepository.findTopByAuctionedProductIdOrderByAmountDesc(ap.getId())
					.ifPresent(winningBid -> ap.setWinningBid(winningBid));
		});

		auctionedProductRepository.saveAll(auctionedProducts);

		// Mark auction ended and save
		auction.setEnded(true);
		auctionRepository.save(auction);

		// Map to generated AuctionClosureDetails model
		AuctionClosureDetails response = new AuctionClosureDetails().auctionId(auction.getId()).name(auction.getName())
				.ended(auction.isEnded());

		List<AuctionClosureDetailsProductsInner> productDetails = auctionedProducts.stream().map(ap -> {
			AuctionClosureDetailsProductsInner prod = new AuctionClosureDetailsProductsInner();
			prod.setName(ap.getProduct().getProductName());
			prod.setMinimumBidAmount(ap.getMinimumBid());
			if (ap.getWinningBid() != null) {
				prod.setWinningBidId(ap.getWinningBid().getId());
				prod.setWinningBidUserId(ap.getWinningBid().getBidderUserId());
				prod.setWinningBidAmount(ap.getWinningBid().getAmount());
			}
			return prod;
		}).toList();

		response.setProducts(productDetails);

		return response;
	}

	public Auction createAuction(AuctionDetails details) {
		Auction auction = modelMapper.map(details, Auction.class);
		auction.setOwnerUid(ThreadContext.get("uid"));
		auction.setEnded(false);
		auctionRepository.save(auction);
		return auction;
	}
}
