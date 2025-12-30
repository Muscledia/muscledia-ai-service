package com.muscledia.muscledia_ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading routine data
 * Single responsibility: data access only
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineDataService {

    private static final String ROUTINES_FILENAME = "public_routines.json";

    private final ResourceLoaderService resourceLoaderService;

    /**
     * Load all public routines from data source
     */
    public String loadAllRoutines() {
        log.debug("Loading all public routines");
        return resourceLoaderService.loadJsonData(ROUTINES_FILENAME);
    }
}