package org.cloudsimplus.examples;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CloudSimSimulation {

    private static final int HOSTS = 2;
    private static final int VMS = 2;
    private static final int CLOUDLETS = 5;

    private static List<Host> hostList;

    public static void main(String[] args) {

        CloudSim simulation = new CloudSim();

        // Create Datacenter and Broker
        Datacenter datacenter = createDatacenter(simulation);
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);

        // Create VMs and Cloudlets
        List<Vm> vmList = createVms();
        List<Cloudlet> cloudletList = createCloudlets();

        // Submit VMs and Cloudlets to broker
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        // Start the simulation
        simulation.start();

        // Print Cloudlet execution results
        System.out.println("\n=== CLOUDLET EXECUTION RESULTS ===");
        cloudletList.forEach(cloudlet -> {
            System.out.printf("Cloudlet %d finished on VM %d with status: %s\n",
                    cloudlet.getId(),
                    cloudlet.getVm().getId(),
                    cloudlet.getStatus());
        });
    }

    private static Datacenter createDatacenter(CloudSim simulation) {
        hostList = new ArrayList<>();

        for (int i = 0; i < HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                peList.add(new PeSimple(1000)); // 1000 MIPS per core
            }

            Host host = new HostSimple(8000, 16000, 1000000, peList);
            host.setVmScheduler(new VmSchedulerTimeShared());

            // Simulate fault tolerance for host 1
            if (i == 1) {
                simulation.addOnClockTickListener(evt -> {
                    if (evt.getTime() == 10) {
                        System.out.println("\nHost 1 fails at time 10");

                        // Disable the host
                        host.setActive(false);

                        // Get list of VMs to migrate
                        List<Vm> vmsToMigrate = host.getVmList().stream()
                                .map(vm -> (Vm) vm)
                                .collect(Collectors.toList());

                        for (Vm vm : vmsToMigrate) {
                            // Find another active host
                            Host targetHost = hostList.stream()
                                    .filter(h -> h.isActive() && !h.equals(host))
                                    .findFirst()
                                    .orElse(null);

                            if (targetHost != null) {
                                System.out.printf("Migrating VM %d to Host %d\n", vm.getId(), hostList.indexOf(targetHost));
                                targetHost.createVm(vm);
                            } else {
                                System.out.printf("No active host available to migrate VM %d\n", vm.getId());
                            }
                        }

                        // Clear the VMs from the failed host
                        host.getVmList().clear();
                    }
                });
            }

            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
    }

    private static List<Vm> createVms() {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {
            Vm vm = new VmSimple(i, 1000, 2)
                    .setRam(2048)
                    .setBw(1000)
                    .setSize(10000)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }

        return vmList;
    }

    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudletList = new ArrayList<>();
        UtilizationModelDynamic utilization = new UtilizationModelDynamic(0.5);

        for (int i = 0; i < CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(10000, 2, utilization);
            cloudlet.setSizes(1024);
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}
