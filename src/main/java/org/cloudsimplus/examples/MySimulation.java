package org.cloudsimplus.examples;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class MySimulation {
    private static final int NUM_HOSTS = 2;
    private static final int NUM_VMS = 4;
    private static final int NUM_CLOUDLETS = 10;

    public static void main(String[] args) {
        // Step 1: Create the simulation environment
        CloudSim simulation = new CloudSim();

        // Step 2: Create two datacenters
        Datacenter dc1 = createDatacenter(simulation, "DC1");
        Datacenter dc2 = createDatacenter(simulation, "DC2");

        // Step 3: Create a broker to manage VMs and cloudlets
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);

        // Step 4: Create and submit VMs
        List<Vm> vmList = createVms();
        broker.submitVmList(vmList);

        // Step 5: Create and submit Cloudlets
        List<Cloudlet> cloudletList = createCloudlets();
        broker.submitCloudletList(cloudletList);

        // Step 6: Run simulation
        simulation.start();

        // Step 7: Show results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        printResults(finishedCloudlets);
    }

    private static Datacenter createDatacenter(CloudSim simulation, String name) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < NUM_HOSTS; i++) {
            List<Pe> peList = List.of(new PeSimple(1000)); // 1 core of 1000 MIPS
            Host host = new HostSimple(2048, 10000, 100000, peList);
            host.setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(host);
        }

        Datacenter dc = new DatacenterSimple(simulation, hostList);
        dc.setName(name);
        return dc;
    }

    private static List<Vm> createVms() {
        List<Vm> list = new ArrayList<>();

        for (int i = 0; i < NUM_VMS; i++) {
            Vm vm = new VmSimple(i, 1000, 1)
                .setRam(512).setBw(1000).setSize(10000);
            list.add(vm);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> list = new ArrayList<>();

        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(5000 + i * 100, 1);
            cloudlet.setUtilizationModelCpu(new UtilizationModelFull());
            list.add(cloudlet);
        }

        return list;
    }

    private static void printResults(List<Cloudlet> cloudlets) {
        System.out.println("\n--- Cloudlet Execution Results ---");
        System.out.printf("%-10s%-10s%-10s%-10s%-10s\n", "Cloudlet", "Status", "VM", "Start", "Finish");

        for (Cloudlet cl : cloudlets) {
            System.out.printf("%-10d%-10s%-10d%-10.2f%-10.2f\n",
                    cl.getId(),
                    cl.getStatus(),
                    cl.getVm().getId(),
                    cl.getExecStartTime(),
                    cl.getFinishTime());
        }
    }
}