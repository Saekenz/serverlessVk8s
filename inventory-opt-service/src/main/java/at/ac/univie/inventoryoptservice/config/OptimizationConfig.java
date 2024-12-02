package at.ac.univie.inventoryoptservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "inv.opt.ga")
public class OptimizationConfig {
    private double mutationRate;
    private int populationSize;
    private int generations;

    public void setConfig(OptimizationConfig config) {
        // Check for default values and replace them with environment variable values
        this.mutationRate = (config.getMutationRate() == 0.0)
                ? Double.parseDouble(System.getenv("inv.opt.ga.mutationRate"))
                : config.getMutationRate();

        this.populationSize = (config.getPopulationSize() == 0)
                ? Integer.parseInt(System.getenv("inv.opt.ga.populationSize"))
                : config.getPopulationSize();

        this.generations = (config.getGenerations() == 0)
                ? Integer.parseInt(System.getenv("inv.opt.ga.generations"))
                : config.getGenerations();
    }

}