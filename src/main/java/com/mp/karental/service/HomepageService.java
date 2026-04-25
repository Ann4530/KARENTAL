package com.mp.karental.service;

import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.entity.Feedback;
import com.mp.karental.mapper.FeedbackMapper;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling homepage-related data retrieval.
 * <p>
 * This service provides methods to fetch:
 * - Latest 4 five-star feedbacks for display on the homepage.
 * - Top 6 cities with the highest number of cars.
 * </p>
 *
 * Author: AnhHP9
 * Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class HomepageService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final CarRepository carRepository;

    /**
     * Retrieves the latest 4 five-star feedbacks for display on the homepage.
     * <p>
     * - Fetches feedbacks with a rating of 5, ordered by creation date (most recent first).
     * - Converts the Feedback entity list to a simplified DTO list using FeedbackMapper.
     * </p>
     *
     * @return HomepageFeedbackResponse containing the latest 4 five-star feedbacks.
     */
    public HomepageFeedbackResponse getHomepageFeedbackData() {
        List<Feedback> latestFiveStarFeedbacks = feedbackRepository.findTop4ByRatingOrderByCreatedDateDesc(PageRequest.of(0, 4));

        return HomepageFeedbackResponse.builder()
                .latestFiveStarFeedbacks(feedbackMapper.toSimpleFeedbackResponseList(latestFiveStarFeedbacks))
                .build();
    }

    /**
     * Retrieves the top 6 cities with the highest number of registered cars.
     * <p>
     * - Fetches the count of cars grouped by city (cityProvince) from the database.
     * - Maps the raw query results into a list of CityCarCount DTOs.
     * </p>
     *
     * @return HomepageCityResponse containing the top 6 cities with the highest car count.
     */
    public HomepageCityResponse getHomepageCityData() {
        // Fetch top 6 cities with the highest car count
        List<Object[]> topCities = carRepository.findTop6CitiesByCarCount();

        // Convert query result into a list of CityCarCount DTOs
        // BRL-06-02: round down car count to nearest 10 (e.g. 67 → 60)
        List<HomepageCityResponse.CityCarCount> cityCarCounts = topCities.stream()
                .map(obj -> {
                    int rawCount = ((Number) obj[1]).intValue();
                    int roundedCount = (rawCount / 10) * 10;
                    return new HomepageCityResponse.CityCarCount((String) obj[0], roundedCount);
                })
                .toList();

        return new HomepageCityResponse(cityCarCounts);
    }
}
