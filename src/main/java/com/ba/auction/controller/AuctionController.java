package com.ba.auction.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.ba.auction.api.AuctionsApi;
import com.ba.auction.entity.Auction;
import com.ba.auction.entity.AuctionedProduct;
import com.ba.auction.model.AuctionClosureDetails;
import com.ba.auction.model.AuctionDetails;
import com.ba.auction.model.AuctionedProductDetails;
import com.ba.auction.model.BidDetails;
import com.ba.auction.service.AuctionService;
import com.ba.auction.service.AuctionedProductService;
import com.ba.auction.service.BidService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuctionController implements AuctionsApi {

	private final AuctionService auctionService;
	private final AuctionedProductService productService;
	private final BidService bidService;
	private final ModelMapper modelMapper;

	@Override
	@PreAuthorize("hasAuthority('auction:create')")
	public ResponseEntity<Void> createAuction(@Valid AuctionDetails auctionDetails) {
		auctionService.createAuction(auctionDetails);
		return ResponseEntity.ok().build();
	}

	@Override
	@PreAuthorize("hasAuthority('auction:view')")
	public ResponseEntity<List<AuctionDetails>> listAuctions(@NotNull @Valid Boolean open) {
		List<Auction> openAuctions = auctionService.getAllActiveAuctions();
		List<AuctionDetails> response = openAuctions.stream().map(a -> modelMapper.map(a, AuctionDetails.class))
				.collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	@Override
	@PreAuthorize("hasAuthority('auction:view')")
	public ResponseEntity<AuctionDetails> viewAuctions(String auctionId) {
		Auction auction = auctionService.getAuctionForOwner(Long.valueOf(auctionId));
		return ResponseEntity.ok(modelMapper.map(auction, AuctionDetails.class));
	}

	@Override
	@PreAuthorize("hasAuthority('auctionedProduct:create')")
	public ResponseEntity<Void> addProductToAnAuction(String auctionId,
			@Valid AuctionedProductDetails auctionedProductDetails) {
		productService.addProductToAuction(Long.valueOf(auctionId), auctionedProductDetails);
		return ResponseEntity.ok().build();
	}

	@Override
	@PreAuthorize("hasAuthority('auction:close')")
	public ResponseEntity<AuctionClosureDetails> closeAuction(Long auctionId) {
		return ResponseEntity.ok().body(auctionService.endAuction(auctionId));
	}

	@Override
	@PreAuthorize("hasAuthority('auctionedProduct:view')")
	public ResponseEntity<List<AuctionedProductDetails>> viewAuctionedProducts(String auctionId) {
		List<AuctionedProduct> products = productService.getAuctionedProducts(Long.valueOf(auctionId));
		List<AuctionedProductDetails> response = products.stream()
				.map(p -> modelMapper.map(p, AuctionedProductDetails.class)).collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	@Override
	@PreAuthorize("hasAuthority('bid:create')")
	public ResponseEntity<Void> registerBids(String auctionId, String productId, @Valid BidDetails bidDetails) {
		bidService.placeBid(Long.valueOf(auctionId), Long.valueOf(productId), bidDetails);
		return ResponseEntity.ok().build();
	}

}
