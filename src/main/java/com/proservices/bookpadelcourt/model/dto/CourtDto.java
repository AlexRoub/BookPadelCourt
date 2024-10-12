package com.proservices.bookpadelcourt.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtDto {

	private Long id;
	private String name;
	private String location;
	private String company;
	private Boolean isActive;
	private String createdAt;
}
