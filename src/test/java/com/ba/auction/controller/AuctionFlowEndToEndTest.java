/**
 * 
 */
package com.ba.auction.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.ba.auction.model.AuctionDetails;
import com.ba.auction.model.AuctionedProductDetails;
import com.ba.auction.model.BidDetails;
import com.ba.users.ApiClient;
import com.ba.users.ApiException;
import com.ba.users.api.DefaultApi;
import com.ba.users.model.UserDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Preeti Pandey
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionFlowEndToEndTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DefaultApi defaultApi;

	private ApiClient mockApiClient;

	private String bearerToken = "Bearer dummy-token";

	private UserDetailsResponse mockUser;

	private ObjectMapper objMapper = new ObjectMapper();

	@BeforeEach
	void setup() throws ApiException {
		mockUser = new UserDetailsResponse();
		mockUser.setId("test-user");
		mockUser.setScopes(List.of("auction:create", "auction:view", "auction:close", "auctionedProduct:create",
				"auctionedProduct:view", "bid:create"));

		mockApiClient = Mockito.mock(ApiClient.class);
		when(defaultApi.getApiClient()).thenReturn(mockApiClient);
		when(mockApiClient.setBearerToken(anyString())).thenReturn(mockApiClient);
		when(defaultApi.authorizeUserToken()).thenReturn(mockUser);

	}

	@Order(1)
	@Test
	void testAuctionFlow() throws Exception {

		AuctionDetails request = new AuctionDetails();
		request.setName("New Auction");

		// create an auction
		MvcResult response = mockMvc
				.perform(post("/auctions").contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(request)).header("Authorization", bearerToken))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.auctionId").exists())
				.andExpect(jsonPath("$.name").value(request.getName())).andReturn();

		AuctionDetails createdAuctionDetails = objMapper.readValue(response.getResponse().getContentAsString(),
				AuctionDetails.class);

		// get the Auction by Id
		mockMvc.perform(
				get("/auctions/{auctionId}", createdAuctionDetails.getAuctionId()).header("Authorization", bearerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctionId").value(createdAuctionDetails.getAuctionId()))
				.andExpect(jsonPath("$.name").value(createdAuctionDetails.getName()));

		// list all the open Auctions
		mockMvc.perform(get("/auctions").param("open", "true").header("Authorization", bearerToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].auctionId").value(createdAuctionDetails.getAuctionId()))
				.andExpect(jsonPath("$[0].name").value(createdAuctionDetails.getName()));

		// add the product 'handbag' to the Auction
		AuctionedProductDetails auctionedHandbag = new AuctionedProductDetails();
		auctionedHandbag.productName("handbag");
		auctionedHandbag.setMinimumBid(Float.valueOf("300"));

		response = mockMvc
				.perform(post("/auctions/{auctionId}/auctionedProducts", createdAuctionDetails.getAuctionId())
						.contentType(MediaType.APPLICATION_JSON).content(objMapper.writeValueAsString(auctionedHandbag))
						.header("Authorization", bearerToken))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.productName").value(auctionedHandbag.getProductName()))
				.andExpect(jsonPath("$.minimumBid").value(auctionedHandbag.getMinimumBid())).andReturn();

		AuctionedProductDetails createdAuctionHandbag = objMapper.readValue(response.getResponse().getContentAsString(),
				AuctionedProductDetails.class);

		// add the product 'vase' to the Auction
		AuctionedProductDetails auctionedVase = new AuctionedProductDetails();
		auctionedVase.productName("vase");
		auctionedVase.setMinimumBid(Float.valueOf("300"));

		response = mockMvc
				.perform(post("/auctions/{auctionId}/auctionedProducts", createdAuctionDetails.getAuctionId())
						.contentType(MediaType.APPLICATION_JSON).content(objMapper.writeValueAsString(auctionedVase))
						.header("Authorization", bearerToken))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.productName").value(auctionedVase.getProductName()))
				.andExpect(jsonPath("$.minimumBid").value(auctionedVase.getMinimumBid())).andReturn();

		AuctionedProductDetails createdAuctionVase = objMapper.readValue(response.getResponse().getContentAsString(),
				AuctionedProductDetails.class);

		// list all the auctioned products in an auction
		mockMvc.perform(get("/auctions/{auctionId}/auctionedProducts", createdAuctionDetails.getAuctionId())
				.header("Authorization", bearerToken)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].id").value(createdAuctionHandbag.getId()))
				.andExpect(jsonPath("$[0].productName").value(createdAuctionHandbag.getProductName()))
				.andExpect(jsonPath("$[1].id").value(createdAuctionVase.getId()))
				.andExpect(jsonPath("$[1].productName").value(createdAuctionVase.getProductName()));

		BidDetails bid = new BidDetails();
		bid.amount(Float.valueOf("50"));

		// place $50 bid on handbag
		mockMvc.perform(
				post("/auctions/{auctionId}/auctionedProducts/{productId}/bids", createdAuctionDetails.getAuctionId(),
						createdAuctionHandbag.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(bid)).header("Authorization", bearerToken))
				.andExpect(status().isCreated());

		// place $50 bid on vase
		mockMvc.perform(
				post("/auctions/{auctionId}/auctionedProducts/{productId}/bids", createdAuctionDetails.getAuctionId(),
						createdAuctionVase.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(bid)).header("Authorization", bearerToken))
				.andExpect(status().isCreated());

		// place $350 bid on handbag
		bid.amount(Float.valueOf("350"));
		mockMvc.perform(
				post("/auctions/{auctionId}/auctionedProducts/{productId}/bids", createdAuctionDetails.getAuctionId(),
						createdAuctionHandbag.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(bid)).header("Authorization", bearerToken))
				.andExpect(status().isCreated());

		// place $350 bid on vase
		mockMvc.perform(
				post("/auctions/{auctionId}/auctionedProducts/{productId}/bids", createdAuctionDetails.getAuctionId(),
						createdAuctionVase.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(bid)).header("Authorization", bearerToken))
				.andExpect(status().isCreated());

		// close the auction
		mockMvc.perform(post("/auctions/{auctionId}/close", createdAuctionDetails.getAuctionId())
				.header("Authorization", bearerToken)).andExpect(status().isOk())
				.andExpect(jsonPath("$.ended").value(true)).andExpect(jsonPath("$.products").isArray())
				.andExpect(jsonPath("$.products[0].name").value(createdAuctionHandbag.getProductName()))
				.andExpect(jsonPath("$.products[0].minimumBidAmount").value(createdAuctionHandbag.getMinimumBid()))
				.andExpect(jsonPath("$.products[0].winningBidAmount").value("350.0"))
				.andExpect(jsonPath("$.products[1].name").value(createdAuctionVase.getProductName()))
				.andExpect(jsonPath("$.products[0].minimumBidAmount").value(createdAuctionVase.getMinimumBid()))
				.andExpect(jsonPath("$.products[0].winningBidAmount").value("350.0"));
	}
}
