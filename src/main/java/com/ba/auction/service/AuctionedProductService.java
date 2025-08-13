package com.ba.auction.service;

import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import com.ba.auction.entity.Auction;
import com.ba.auction.entity.AuctionedProduct;
import com.ba.auction.entity.Product;
import com.ba.auction.exception.NotFoundException;
import com.ba.auction.model.AuctionedProductDetails;
import com.ba.auction.repository.AuctionRepository;
import com.ba.auction.repository.AuctionedProductRepository;
import com.ba.auction.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionedProductService {

	private final AuctionedProductRepository auctionedProductRepository;
	private final AuctionRepository auctionRepository;
	private final ProductRepository productrepository;

	public AuctionedProduct addProductToAuction(Long auctionId, AuctionedProductDetails productDetail) {
		Auction auction = auctionRepository.findByIdAndOwnerUid(auctionId, ThreadContext.get("uid"))
				.orElseThrow(() -> new NotFoundException("Auction not found"));

		Product product = productrepository.findByProductName(productDetail.getProductName())
				.orElseThrow(() -> new NotFoundException("Product not found"));

		AuctionedProduct auctionedProduct = new AuctionedProduct();
		auctionedProduct.setAuction(auction);
		auctionedProduct.setProduct(product);
		auctionedProduct.setMinimumBid(productDetail.getMinimumBid());
		auctionedProductRepository.save(auctionedProduct);
		return auctionedProduct;
	}

	public List<AuctionedProduct> getAuctionedProducts(Long auctionId) {
		return auctionedProductRepository.findByAuctionId(auctionId);

	}

}
