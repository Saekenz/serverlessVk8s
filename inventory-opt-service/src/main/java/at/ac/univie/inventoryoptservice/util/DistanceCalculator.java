package at.ac.univie.inventoryoptservice.util;

import at.ac.univie.inventoryoptservice.optimization.LocationPair;
import at.ac.univie.inventoryoptservice.optimization.SimpleLocation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DistanceCalculator {
    private static final double EARTH_RADIUS = 6378.0;

    // https://en.wikipedia.org/wiki/Haversine_formula
    private double calculateDistanceBetweenLocationsInKm(double lat1, double lon1, double lat2, double lon2) {
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double havTheta = Math.pow(Math.sin(deltaPhi / 2), 2) + Math.cos(phi1) * Math.cos(phi2) *
                Math.pow(Math.sin(deltaLambda / 2), 2);

        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(havTheta));
    }

    // calculate distance between locations for all possible location combinations
    public Map<LocationPair, Double> calculateDistanceMatrix(List<SimpleLocation> locations) {
        Map<LocationPair, Double> distanceMatrix = new HashMap<>();

        for (int i = 0; i < locations.size() - 1; i++) {
            SimpleLocation fromLocation = locations.get(i);
            for (int j = i + 1; j < locations.size(); j++) {
                SimpleLocation toLocation = locations.get(j);
                double distance = calculateDistanceBetweenLocationsInKm(fromLocation.getLatitude(),
                        fromLocation.getLongitude(), toLocation.getLatitude(), toLocation.getLongitude());
                distanceMatrix.put(new LocationPair(fromLocation.getLocationId(),
                        toLocation.getLocationId()), distance);
            }
        }
        return distanceMatrix;
    }
}
