/**
 * 
 */
package com.ba.auction.configuration;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.record.RecordModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Preeti Pandey
 *
 */
@Configuration
public class AppCustomConfiguration {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.registerModule(new RecordModule());
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setSkipNullEnabled(true)
				.setMatchingStrategy(MatchingStrategies.LOOSE);
		return modelMapper;
	}

}
