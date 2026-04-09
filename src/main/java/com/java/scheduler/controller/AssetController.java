package com.java.scheduler.controller;

import com.java.scheduler.domain.Asset;
import com.java.scheduler.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
// Use specific origin for security
@CrossOrigin(origins = "http://localhost:5173") 
public class AssetController {

    private final AssetService assetService;

    // 1. ADD ASSET
    @PostMapping
    public ResponseEntity<Asset> addAsset(@RequestBody Asset asset) {
        assetService.create(asset);
        return new ResponseEntity<>(asset, HttpStatus.CREATED);
    }

    // 2. GET ALL ASSETS
    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetService.showAll());
    }

    // 3. GET SINGLE ASSET
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAsset(@PathVariable int id) {
        Asset asset = assetService.read(id);
        return asset != null ? ResponseEntity.ok(asset) : ResponseEntity.notFound().build();
    }

    // 4. UPDATE ASSET (Returning the updated asset is better for React state)
    @PutMapping
    public ResponseEntity<Asset> updateAsset(@RequestBody Asset asset) {
        assetService.update(asset);
        return ResponseEntity.ok(asset);
    }

    // 5. DELETE ASSET
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAsset(@PathVariable int id) {
        assetService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Asset deleted successfully"));
    }

    // 6. DASHBOARD STATS
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
            "total", assetService.totalAssets(),
            "available", assetService.availableAssets()
        ));
    }
}