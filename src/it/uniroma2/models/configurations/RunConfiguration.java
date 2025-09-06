package it.uniroma2.models.configurations;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class RunConfiguration {
    @Getter@Setter String name;
    @Getter@Setter Map<String, String> params;

    public RunConfiguration(String name) {
        this.name = name;
        this.params = new HashMap<>();
    }

    public RunConfiguration(String name, Map<String, String> params) {
        this.name = name;
        this.params = new HashMap<>(params);
    }

    public void put(String key, String value) {
        this.params.put(key, value);
    }

    public String get(String key) {
        return this.params.get(key);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("name='").append(name).append("'");
        sb.append("\nparams={");
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append("\n\t\t").append(entry.getKey()).append('=').append(entry.getValue());
            }
        }
        sb.append("\n}");
        return sb.toString();
    }

    public String getDescription() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(" {");
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().contains("infrastructure") || entry.getKey().contains("webserver") || entry.getKey().contains("distribution")){
                    String shortKey = entry.getKey()
                            .replace("infrastructure.", "i.")
                            .replace("webserver.", "ws.")
                            .replace("distribution.", "d.");
                    sb
                            .append(shortKey)
                            .append('=')
                            .append(entry.getValue())
                            .append("; ");
                }
            }
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("}");
        return sb.toString();
    }
}
