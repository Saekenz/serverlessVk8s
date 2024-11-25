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

    /**
     * Calculates distance in km for every {@link SimpleLocation} contained in the given {@link List}.
     *
     * @param locations {@link List} containing the {@link SimpleLocation} objects.
     * @return Distance in km between every {@link SimpleLocation} location pair that can be created from the given
     * {@link List} (if location pair consists of the same {@link SimpleLocation} the calculated distance is 0.0).
     */
    public Map<LocationPair, Double> calculateDistanceMatrix(List<SimpleLocation> locations) {
        Map<LocationPair, Double> distanceMatrix = new HashMap<>();

        // allow distance calculation between same location (results in distance = 0.0)
        for (int i = 0; i < locations.size(); i++) {
            SimpleLocation fromLocation = locations.get(i);
            for (int j = i; j < locations.size(); j++) {
                SimpleLocation toLocation = locations.get(j);
                double distance = calculateDistanceBetweenLocationsInKm(fromLocation.getLatitude(),
                        fromLocation.getLongitude(), toLocation.getLatitude(), toLocation.getLongitude());
                distanceMatrix.put(new LocationPair(fromLocation.getLocationId(),
                        toLocation.getLocationId()), distance);
            }
        }
        return distanceMatrix;
    }

    public double calculateMaxPossibleDistance(Map<LocationPair, Double> distanceMatrix) {
        double maxDistance = 0;
        for (LocationPair entry : distanceMatrix.keySet()) {
            maxDistance += distanceMatrix.get(entry);
        }
        return maxDistance;
    }
}
