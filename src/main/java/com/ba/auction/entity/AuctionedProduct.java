package com.ba.auction.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction_product")
public class AuctionedProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(name = "minimum_bid")
	private double minimumBid;

	@ManyToOne
	@JoinColumn(name = "auction_id")
	private Auction auction;

	@OneToOne
	@JoinColumn(name = "winning_bid_id")
	private Bid winningBid;

	@OneToMany(mappedBy = "auctionedProduct", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bid> bids;
}
