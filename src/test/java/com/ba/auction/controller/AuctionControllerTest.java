package com.ba.auction.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ba.auction.entity.Auction;
import com.ba.auction.entity.AuctionedProduct;
import com.ba.auction.entity.Bid;
import com.ba.auction.entity.Product;
import com.ba.auction.repository.AuctionRepository;
import com.ba.auction.repository.AuctionedProductRepository;
import com.ba.auction.repository.BidRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionedProductRepository auctionedProductRepository;

    @Autowired
    private BidRepository bidRepository;

    private Auction auction;
    private AuctionedProduct product1;
    private AuctionedProduct product2;

    private String bearerToken = "Bearer dummy-token";

    @BeforeEach
    public void setup() {
        auction = new Auction();
        auction.setName("Test Auction");
        auction.setEnded(false);
        auction.setOwnerUid("test-user");
        auctionRepository.save(auction);

        product1 = new AuctionedProduct();
        product1.setAuction(auction);
        product1.setMinimumBid(100);
        product1.setProduct(new Product(1L, "Product 1"));
        auctionedProductRepository.save(product1);

        product2 = new AuctionedProduct();
        product2.setAuction(auction);
        product2.setMinimumBid(200);
        product2.setProduct(new Product(2L, "Product 2"));
        auctionedProductRepository.save(product2);

        Bid bid1 = new Bid();
        bid1.setAuctionedProduct(product1);
        bid1.setBidderUserId("bidder1");
        bid1.setAmount(150);
        bidRepository.save(bid1);

        Bid bid2 = new Bid();
        bid2.setAuctionedProduct(product1);
        bid2.setBidderUserId("bidder2");
        bid2.setAmount(180);
        bidRepository.save(bid2);

        Bid bid3 = new Bid();
        bid3.setAuctionedProduct(product2);
        bid3.setBidderUserId("bidder3");
        bid3.setAmount(250);
        bidRepository.save(bid3);
    }

    @Test
    public void testListAuctions_openTrue() throws Exception {
        mockMvc.perform(get("/api/v1/auctions")
                .param("open", "true")
                .header("Authorization", bearerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].auctionId").value(auction.getId().toString()))
            .andExpect(jsonPath("$[0].name").value("Test Auction"));
    }

    @Test
    public void testCreateAuction() throws Exception {
        String json = """
            {
              "name": "New Auction"
            }
        """;

        mockMvc.perform(post("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", bearerToken))
            .andExpect(status().isCreated());
    }

    @Test
    public void testGetAuctionById() throws Exception {
        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auction.getId())
                .header("Authorization", bearerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.auctionId").value(auction.getId().toString()))
            .andExpect(jsonPath("$.name").value("Test Auction"));
    }

    @Test
    public void testCloseAuction() throws Exception {
        mockMvc.perform(put("/api/v1/auctions/{auctionId}", auction.getId())
                .header("Authorization", bearerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ended").value(true))
            .andExpect(jsonPath("$.auctionedProducts").isArray());
    }

    @Test
    public void testGetAuctionedProducts() throws Exception {
        mockMvc.perform(get("/api/v1/auctions/{auctionId}/auctionedProducts", auction.getId())
                .header("Authorization", bearerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].productName").value("Product 1"));
    }

    @Test
    public void testAddProductToAuction() throws Exception {
        String productJson = """
        {
          "productName": "Product 3",
          "minimumBid": 300
        }
        """;

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/auctionedProducts", auction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson)
                .header("Authorization", bearerToken))
            .andExpect(status().isCreated());
    }

    @Test
    public void testPlaceBid() throws Exception {
        String bidJson = """
        {
          "bidderName": "bidder4",
          "amount": 400
        }
        """;

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/auctionedProducts/{productId}/bids", auction.getId(), product1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bidJson)
                .header("Authorization", bearerToken))
            .andExpect(status().isCreated());
    }
}
