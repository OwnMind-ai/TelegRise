package org.telegrise.telegrise.core;

import lombok.Setter;
import org.telegrise.telegrise.Service;
import org.telegrise.telegrise.resources.ResourceInjector;

import java.util.HashSet;
import java.util.Set;

public class ServiceManager {
    private final ThreadGroup threadGroup = new ThreadGroup("TelegRiseServices");
    private final Set<Service> services = new HashSet<>();
    @Setter
    private ResourceInjector injector;

    public void startServices(){
        this.services.forEach(this::runService);
    }

    private void runService(Service service){
        this.injector.injectResources(service);

        Thread serviceThread = new Thread(this.threadGroup, service);

        if (service.threadPriority() != null)
            serviceThread.setPriority(service.threadPriority());

        serviceThread.start();
    }

    public void stop(){
        threadGroup.interrupt();
    }

    public void add(Service service){
        this.services.add(service);
    }
}
