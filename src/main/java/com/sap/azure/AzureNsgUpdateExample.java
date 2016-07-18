/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.azure;

import com.microsoft.azure.Azure;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 *
 * @author i330120
 */
public class AzureNsgUpdateExample {

    public static void main(String args[]) throws Exception {

        // Login credentials
        String clientId = "YOUR_CLIENT_ID"; // AppId
        String domain = "YOUR_TENANT_ID"; // TenantId/UserId
        String secret = "APP_SECRET"; // App password

        String subscription = "YOUR_SUBSCRIPTION";
        String resGroup = "YOUR_RESOURCE_GROUP";
        String vmName = "YOUR_VM_NAME";

        ServiceClientCredentials srvcCreds = new ApplicationTokenCredentials(
                clientId, domain, secret, AzureEnvironment.AZURE);

        Azure az = Azure.authenticate(srvcCreds).withSubscription(subscription);

        // Get VM view
        VirtualMachine vm = az.virtualMachines().getByGroup(resGroup, vmName);

        NetworkSecurityGroup nsg = null;

        // Find primary nic and get nsg
//        for (String nref : vm.networkInterfaceIds()) {
//            NetworkInterface nic = az.networkInterfaces().getById(nref);
//            if ((nic != null) && (nic.isPrimary())) {
//                nsg = az.networkSecurityGroups().getById(nic.networkSecurityGroup().id());
//                break;
//            }
//        }

        NetworkInterface nic = vm.primaryNetworkInterface();
        nsg = az.networkSecurityGroups().getById(nic.networkSecurityGroupId());

        if (nsg == null) {
            System.err.println("No NSG found exiting");
            System.exit(1);
        }

        nsg.update().defineRule("inbound_ssh")
                .allowInbound()
                .fromAnyAddress()
                .fromPort(22)
                .toAnyAddress()
                .toPort(22)
                .withProtocol(NetworkSecurityRule.Protocol.TCP)
                .withDescription("Inbound SSH")
                .withPriority(123)
                .attach()
                .apply();


        nsg.update().defineRule("outbound_ssh")
                .allowOutbound()
                .fromAnyAddress()
                .fromPort(22)
                .toAnyAddress()
                .toPort(22)
                .withProtocol(NetworkSecurityRule.Protocol.TCP)
                .withDescription("Outbound SSH")
                .withPriority(123)
                .attach()
                .apply();

        System.out.println("");
    }
}
