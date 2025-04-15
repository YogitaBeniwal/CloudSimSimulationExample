package com.cloudsim;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;

import java.util.List;

public class App {
    public static void main(String[] args) {
        CloudSim simulation = new CloudSim();

        var datacenter = createDatacenter(simulation);
        var broker = new DatacenterBrokerSimple(simulation);

        var vms = createVms();
        var cloudlets = createCloudlets();

        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);

        simulation.start();

        cloudlets.forEach(cl -> {
            System.out.printf("Cloudlet %d finished on VM %d with status %s%n",
                cl.getId(), cl.getVm().getId(), cl.getStatus());
        });
    }

    private static DatacenterSimple createDatacenter(CloudSim simulation) {
        List<Pe> peList = List.of(new PeSimple(1000)); // Correct use of PeSimple
        var host = new HostSimple(2048, 10000, 1000000, peList);
        host.setVmScheduler(new VmSchedulerTimeShared());

        return new DatacenterSimple(simulation, List.of(host));
    }

    private static List<Vm> createVms() {
        Vm vm = new VmSimple(1000, 1);
        vm.setRam(512).setBw(1000).setSize(10000);
        return List.of(vm);
    }

    private static List<Cloudlet> createCloudlets() {
        Cloudlet cloudlet = new CloudletSimple(10000, 1);
        cloudlet.setFileSize(300).setOutputSize(300);
        return List.of(cloudlet);
    }
}