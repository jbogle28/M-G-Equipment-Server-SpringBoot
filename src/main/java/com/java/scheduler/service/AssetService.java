package com.java.scheduler.service;

import com.java.scheduler.domain.Asset;
import com.java.scheduler.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    private final AssetRepository assetRepository;

    public Asset create(Asset asset) {
        logger.info("Created asset: {}", asset);
        return assetRepository.save(asset);
    }

    public Asset read(int assetId) {
        logger.info("Reading asset with ID: {}", assetId);
        return assetRepository.findById(assetId)
                .orElseGet(() -> {
                    logger.warn("Asset with ID {} not found.", assetId);
                    return null;
                });
    }

    public void update(Asset asset) {
        logger.info("Updating asset: {}", asset);
        if (assetRepository.existsById(asset.getAssetId())) {
            assetRepository.save(asset); 
            logger.info("Asset with ID {} updated successfully.", asset.getAssetId());
        } else {
            logger.warn("Asset with ID {} not found for update.", asset.getAssetId());
        }
    }

    public void delete(int assetId) {
        logger.info("Deleting asset with ID: {}", assetId);
        if (assetRepository.existsById(assetId)) {
            assetRepository.deleteById(assetId);
            logger.info("Asset with ID {} deleted successfully.", assetId);
        } else {
            logger.warn("Asset with ID {} not found for deletion.", assetId);
        }
    }

    public List<Asset> showAll() {
        logger.info("Showing all assets...");
        List<Asset> assets = assetRepository.findAll();
        logger.info("Total assets found: {}", assets.size());
        return assets;
    }


    public int totalAssets() {
        logger.info("Fetching total number of assets...");
        return (int) assetRepository.count();
    }


    public int availableAssets() {
        logger.info("Fetching total number of available assets...");
        // Uses the custom query method in AssetRepository
        return (int) assetRepository.countByStatus(Asset.Status.AVAILABLE);
    }
}