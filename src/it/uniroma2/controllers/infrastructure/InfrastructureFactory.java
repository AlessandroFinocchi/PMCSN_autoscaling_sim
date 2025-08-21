package it.uniroma2.controllers.infrastructure;

import static it.uniroma2.models.Config.SPIKESERVER_ACTIVE;

public final class InfrastructureFactory {
    private InfrastructureFactory() {}

    public static IServerInfrastructure create() {
        IServerInfrastructure instance = new BaseServerInfrastructure();

        if (SPIKESERVER_ACTIVE)
            instance = new SpikedInfrastructureDecorator((BaseServerInfrastructure)instance);

        return instance;
    }
}
