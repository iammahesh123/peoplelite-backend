package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetStatsResponse {

    private long available;
    private long assigned;
    private long returned;
    private long damaged;
    private long lost;
    private long total;
}
