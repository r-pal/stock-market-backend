package com.goblinbank.web.dto;

import java.util.List;

public record HistorySeriesDto(Long houseId, String houseName, List<HistoryPointDto> points) {}
