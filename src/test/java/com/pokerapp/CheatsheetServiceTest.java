package com.pokerapp;

import com.pokerapp.api.dto.request.ChipInventoryDto;
import com.pokerapp.api.dto.response.ChipDistributionDto;
import com.pokerapp.service.spadehub.CheatsheetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CheatsheetServiceTest {

    @InjectMocks
    private CheatsheetService cheatsheetService;

    @Test
    public void testExactMatchDistribution() {
        // This test checks for a case where an exact distribution is possible
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(600);
        inventory.setChip5(120);
        inventory.setChip10(60);
        inventory.setChip25(24);
        inventory.setChip100(6);
        inventory.setChip500(0);
        inventory.setTargetValue(1000);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertTrue(result.isSuccess());
        assertEquals(1000, result.getValuePerPlayer());
        assertEquals(100, result.getEfficiency());
    }

    @Test
    public void testApproximateMatchDistribution() {
        // This test checks for a case where an approximate distribution is the best option
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(10);
        inventory.setChip5(20);
        inventory.setChip10(10);
        inventory.setChip25(5);
        inventory.setChip100(1);
        inventory.setChip500(0);
        inventory.setTargetValue(500);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertTrue(result.isSuccess());

        // Verify the approximate match by recalculating the actual value
        Map<String, Integer> dist = result.getPlayerDistribution();
        int actualValue =
                dist.get("chip1") +
                        dist.get("chip5") * 5 +
                        dist.get("chip10") * 10 +
                        dist.get("chip25") * 25 +
                        dist.get("chip100") * 100 +
                        dist.get("chip500") * 500;

        // The efficiency should reflect how close we got (e.g., 99%)
        assertEquals(result.getEfficiency(), (int)Math.round((double)actualValue / 500 * 100));

        // Value should be within 5 chips (1% tolerance)
        assertTrue(Math.abs(actualValue - 500) <= 5);
    }

    @Test
    public void testSmallTargetValue() {
        // Test with a very small target value (previously problematic)
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(100);
        inventory.setChip5(20);
        inventory.setChip10(10);
        inventory.setChip25(0);
        inventory.setChip100(0);
        inventory.setChip500(0);
        inventory.setTargetValue(50);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testInsufficientChips() {
        // Test with insufficient chips to meet the target
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(10);
        inventory.setChip5(5);
        inventory.setChip10(0);
        inventory.setChip25(0);
        inventory.setChip100(0);
        inventory.setChip500(0);
        inventory.setTargetValue(1000);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertFalse(result.isSuccess());
        assertEquals("Total value is less than the target value per player", result.getError());
    }

    @Test
    public void testLargeNumbers() {
        // Test with large numbers to ensure no overflow
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(1000);
        inventory.setChip5(500);
        inventory.setChip10(200);
        inventory.setChip25(100);
        inventory.setChip100(50);
        inventory.setChip500(20);
        inventory.setTargetValue(5000);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testDistributionQuality() {
        // Test the quality of distribution - prefer higher denomination chips
        ChipInventoryDto inventory = new ChipInventoryDto();
        inventory.setChip1(500);
        inventory.setChip5(100);
        inventory.setChip10(50);
        inventory.setChip25(20);
        inventory.setChip100(10);
        inventory.setChip500(2);
        inventory.setTargetValue(1000);

        ChipDistributionDto result = cheatsheetService.calculateOptimalChipDistribution(inventory);

        assertTrue(result.isSuccess());

        // Should prefer including higher denominations
        Map<String, Integer> dist = result.getPlayerDistribution();
        assertTrue(dist.get("chip100") > 0 || dist.get("chip25") > 0);
    }
}